// app/src/main/java/com/example/quotex/service/ProverbService.kt
package com.example.quotex.service

import android.animation.ValueAnimator
import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.quotex.R
import com.example.quotex.data.repository.ProverbsRepository
import com.example.quotex.data.repository.UserPreferencesRepository
import com.example.quotex.model.Quote
import com.example.quotex.ui.main.MainActivity
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class ProverbService : Service() {

    companion object {
        const val channelId = "quotex_channel"
        const val notificationId = 1
        const val tag = "ProverbService"

        // Broadcast action for service control
        const val ACTION_START_SERVICE = "com.example.quotex.START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.example.quotex.STOP_SERVICE"
        const val ACTION_UPDATE_DISPLAY_MODE = "com.example.quotex.UPDATE_DISPLAY_MODE"
        const val EXTRA_DISPLAY_MODE = "display_mode"

        // Flag to track if service is running
        @Volatile
        var isServiceRunning = false
    }

    @Inject
    lateinit var proverbsRepository: ProverbsRepository

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private lateinit var windowManager: WindowManager
    private var proverbView: View? = null
    private var displayMode = 0 // 0 = Off, 1 = Screen On, 2 = Unlock
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val screenReceiver = ScreenReceiver()
    private var lastQuoteTime = 0L // Track when the last quote was shown

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "Service onCreate")
        createNotificationChannel()

        try {
            startForeground(notificationId, createNotification())
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
                // Add custom actions
                addAction(ACTION_UPDATE_DISPLAY_MODE)
                addAction(ACTION_STOP_SERVICE)
            }
            registerReceiver(screenReceiver, filter, RECEIVER_NOT_EXPORTED)

            serviceScope.launch {
                displayMode = userPreferencesRepository.displayMode.first()
                Log.d(tag, "Initial display mode from repository: $displayMode")
            }

            isServiceRunning = true
        } catch (e: Exception) {
            Log.e(tag, "Service initialization failed: ${e.message}", e)
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(tag, "Service onStartCommand: ${intent?.action}")

        when (intent?.action) {
            ACTION_START_SERVICE -> {
                // Just ensure we're running
                Log.d(tag, "Received START_SERVICE action")
            }
            ACTION_STOP_SERVICE -> {
                Log.d(tag, "Received STOP_SERVICE action, stopping service")
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_UPDATE_DISPLAY_MODE -> {
                val newMode = intent.getIntExtra(EXTRA_DISPLAY_MODE, -1)
                if (newMode >= 0) {
                    displayMode = newMode
                    Log.d(tag, "Updated display mode to: $displayMode")
                }
            }
            else -> {
                // Handle regular intent
                intent?.let {
                    val receivedMode = it.getIntExtra("display_mode", -1)
                    if (receivedMode >= 0) {
                        displayMode = receivedMode
                        Log.d(tag, "Display mode received from intent: $displayMode")
                    }
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "Service onDestroy")
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            Log.e(tag, "Error unregistering receiver: ${e.message}")
        }
        hideProverb()
        serviceScope.cancel()
        isServiceRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Quotes",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
                description = "Shows daily wisdom quotes on your lock screen"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): android.app.Notification {
        // Create intent to open MainActivity when notification is tapped
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.service_notification_title))
            .setContentText(getString(R.string.service_notification_text))
            .setSmallIcon(R.drawable.ic_scroll)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun showProverb() {
        if (displayMode == 0) {
            Log.d(tag, "Display mode is off (0), not showing proverb")
            return
        }

        if (!Settings.canDrawOverlays(this)) {
            Log.e(tag, "Cannot show proverb: no overlay permission")
            return
        }

        // Prevent showing quotes too frequently (at least 2 seconds apart)
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastQuoteTime < 2000) {
            Log.d(tag, "Preventing quote display - too soon after last quote")
            return
        }

        lastQuoteTime = currentTime

        Log.d(tag, "Preparing to show proverb with display mode: $displayMode")

        // For unlock mode, add a longer delay to ensure proper display
        serviceScope.launch {
            if (displayMode == 2) {
                // Increased delay for unlock mode (from 700ms to 1500ms)
                Log.d(tag, "Adding longer delay for unlock mode")
                delay(1500)

                // Double-check if we should still show the quote
                if (!isScreenOn() || !isDeviceUnlocked()) {
                    Log.d(tag, "Device state changed during delay, aborting quote display")
                    return@launch
                }
            }

            // Get and display the quote
            try {
                val quote = proverbsRepository.getRandomProverbForCurrentDay()
                if (quote != null) {
                    Log.d(tag, "Displaying quote: ${quote.text.take(20)}...")
                    displayQuote(quote)
                } else {
                    Log.e(tag, "No quote available")
                }
            } catch (e: Exception) {
                Log.e(tag, "Error getting quote: ${e.message}", e)
            }
        }
    }

    private fun displayQuote(quote: Quote) {
        try {
            // Remove any existing view first
            if (proverbView != null) {
                try {
                    windowManager.removeView(proverbView)
                } catch (e: Exception) {
                    Log.e(tag, "Error removing existing view: ${e.message}")
                }
                proverbView = null
            }

            val inflater = LayoutInflater.from(this)
            proverbView = inflater.inflate(R.layout.lock_screen_proverb, null)

            val proverbText = proverbView?.findViewById<TextView>(R.id.proverb_text)
            val proverbReference = proverbView?.findViewById<TextView>(R.id.proverb_reference)

            proverbText?.text = quote.text
            proverbReference?.text = quote.reference

            // IMPROVED WINDOW PARAMETERS
            val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }

            // Use different flags based on Android version
            var flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            } else {
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                windowType,
                flags,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
                // Ensure proper layering in the window stack
                flags = flags or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                // Use highest possible layer
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }

            Log.d(tag, "Adding proverb view to window")
            windowManager.addView(proverbView, params)

            // Add a glowing animation effect
            val textView = proverbText
            val valueAnimator = ValueAnimator.ofFloat(0.8f, 1.0f)
            valueAnimator.duration = 2000
            valueAnimator.repeatCount = ValueAnimator.INFINITE
            valueAnimator.repeatMode = ValueAnimator.REVERSE
            valueAnimator.addUpdateListener { animator ->
                val value = animator.animatedValue as Float
                textView?.alpha = value
            }
            valueAnimator.start()

            // Auto-hide after a longer delay (20 seconds instead of 15)
            serviceScope.launch {
                delay(20000)
                valueAnimator.cancel()
                hideProverb()
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to add proverb view: ${e.message}", e)
            e.printStackTrace()
        }
    }

    private fun hideProverb() {
        try {
            proverbView?.let { view ->
                mainHandler.post {
                    try {
                        windowManager.removeView(view)
                        Log.d(tag, "Proverb view removed")
                    } catch (e: Exception) {
                        Log.e(tag, "Error removing proverb view: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error removing proverb view: ${e.message}")
        }
        proverbView = null
    }

    private fun isScreenOn(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            powerManager.isInteractive
        } else {
            @Suppress("DEPRECATION")
            (getSystemService(Context.POWER_SERVICE) as android.os.PowerManager).isScreenOn
        }
    }

    private fun isDeviceUnlocked(): Boolean {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return !keyguardManager.isKeyguardLocked
    }

    inner class ScreenReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            Log.d(tag, "Screen event: $action, display mode: $displayMode")

            when (action) {
                Intent.ACTION_SCREEN_ON -> {
                    if (displayMode == 1) {
                        Log.d(tag, "Screen on with display mode 1, showing proverb")
                        // Add a small delay to ensure the screen is fully on
                        mainHandler.postDelayed({ showProverb() }, 500)
                    }
                }
                Intent.ACTION_USER_PRESENT -> {
                    // Handle unlock event with improved reliability
                    if (displayMode == 2) {
                        Log.d(tag, "User present with display mode 2, showing proverb")
                        // Post with a delay to make sure the lock screen is fully dismissed
                        mainHandler.postDelayed({ showProverb() }, 300)
                    }
                }
                Intent.ACTION_SCREEN_OFF -> {
                    hideProverb()
                }
                ACTION_UPDATE_DISPLAY_MODE -> {
                    val newMode = intent.getIntExtra(EXTRA_DISPLAY_MODE, -1)
                    if (newMode >= 0) {
                        displayMode = newMode
                        Log.d(tag, "Updated display mode via broadcast: $displayMode")
                    }
                }
            }
        }
    }
}