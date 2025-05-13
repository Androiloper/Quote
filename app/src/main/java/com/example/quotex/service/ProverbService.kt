// app/src/main/java/com/example/quotex/service/ProverbService.kt
package com.example.quotex.service

import android.animation.ValueAnimator
import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
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
import kotlinx.coroutines.launch
import com.example.quotex.R
import com.example.quotex.data.repository.ProverbsRepository
import com.example.quotex.data.repository.UserPreferencesRepository
import com.example.quotex.model.Quote
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class ProverbService : Service() {

    companion object {
        const val channelId = "quotex_channel"
        const val notificationId = 1
        const val tag = "ProverbService"
    }

    @Inject
    lateinit var proverbsRepository: ProverbsRepository

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private lateinit var windowManager: WindowManager
    private var proverbView: View? = null
    private var displayMode = 0 // 0 = Off, 1 = Screen On, 2 = Unlock
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val screenReceiver = ScreenReceiver()

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
            }
            registerReceiver(screenReceiver, filter)

            serviceScope.launch {
                displayMode = userPreferencesRepository.displayMode.first()
            }
        } catch (e: Exception) {
            Log.e(tag, "Service initialization failed: ${e.message}", e)
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(tag, "Service onStartCommand")
        intent?.let {
            displayMode = it.getIntExtra("displayMode", 0)
            Log.d(tag, "Display mode received: $displayMode")
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "Service onDestroy")
        unregisterReceiver(screenReceiver)
        hideProverb()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Quotes",
                NotificationManager.IMPORTANCE_LOW
            ).apply { setShowBadge(false) }

            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun createNotification(): android.app.Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.service_notification_title))
            .setContentText(getString(R.string.service_notification_text))
            .setSmallIcon(R.drawable.ic_scroll)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /*
    private fun showProverb() {
        if (displayMode == 0) {
            Log.d(tag, "Display mode is off (0), not showing proverb")
            return
        }

        if (!Settings.canDrawOverlays(this)) {
            Log.e(tag, "Cannot show proverb: no overlay permission")
            return
        }

        Log.d(tag, "Starting to show proverb with display mode: $displayMode")

        // Add a delay for unlock mode to ensure it appears after system UI
        serviceScope.launch {
            if (displayMode == 2) {
                // Add delay for unlock mode
                kotlinx.coroutines.delay(500)
            }

            try {
                val quote = proverbsRepository.getRandomProverbForCurrentDay()
                if (quote != null) {
                    Log.d(tag, "Retrieved quote: ${quote.text.take(20)}...")
                    displayQuote(quote)
                } else {
                    Log.e(tag, "Failed to get a quote")
                }
            } catch (e: Exception) {
                Log.e(tag, "Error in showProverb: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }

     */

    // Fix the showProverb method for better unlock handling
    private fun showProverb() {
        if (displayMode == 0 || !Settings.canDrawOverlays(this)) {
            Log.d(tag, "Cannot show proverb: mode=${displayMode}, overlay permission=${Settings.canDrawOverlays(this)}")
            return
        }

        Log.d(tag, "Preparing to show proverb with display mode: $displayMode")

        // IMPORTANT: For unlock mode, add delay to ensure it appears properly
        serviceScope.launch {
            if (displayMode == 2) {
                // This delay is crucial for unlock visibility - gives system UI time to settle
                Log.d(tag, "Adding delay for unlock mode")
                kotlinx.coroutines.delay(700)
            }

            // Get and display the quote
            try {
                val quote = proverbsRepository.getRandomProverbForCurrentDay()
                if (quote != null) {
                    Log.d(tag, "Displaying quote after unlock: ${quote.text.take(20)}...")
                    displayQuote(quote)
                } else {
                    Log.e(tag, "No quote available")
                }
            } catch (e: Exception) {
                Log.e(tag, "Error getting quote: ${e.message}", e)
            }
        }
    }
    private fun fetchAndShowProverb() {
        serviceScope.launch {
            try {
                val quote = proverbsRepository.getRandomProverbForCurrentDay()
                if (quote != null) {
                    displayQuote(quote)
                } else {
                    Log.w(tag, "No quote available to display")
                }
            } catch (e: Exception) {
                Log.e(tag, "Error fetching quote: ${e.message}", e)
            }
        }
    }

    // In ProverbService.kt - updating the displayQuote method

    // Update this method to use correct layout and improve display
    private fun displayQuote(quote: Quote) {
        try {
            if (proverbView != null) {
                try {
                    windowManager.removeView(proverbView)
                } catch (e: Exception) {
                    Log.e(tag, "Error removing existing view: ${e.message}")
                }
                proverbView = null
            }

            val inflater = LayoutInflater.from(this)
            // Use the correct layout
            proverbView = inflater.inflate(R.layout.lock_screen_proverb, null)

            val proverbText = proverbView?.findViewById<TextView>(R.id.proverb_text)
            val proverbReference = proverbView?.findViewById<TextView>(R.id.proverb_reference)

            proverbText?.text = quote.text
            proverbReference?.text = quote.reference

            /*
            // Improved window parameters for better visibility
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
                // Ensure the view has proper z-order
                flags = flags or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                // Add animation for smoother appearance
                windowAnimations = android.R.style.Animation_Toast
            }

             */

            // CRITICAL FIX: Proper window parameters for lock screen visibility
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, // Better for system overlays
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or  // Critical for unlock visibility
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,      // Ensures visibility
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
                // Ensure proper layering in the window stack
                flags = flags or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
            }

            Log.d(tag, "Adding futuristic proverb view to window")
            windowManager.addView(proverbView, params)

            // Add a glow animation effect
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

            // Auto-hide after delay
            serviceScope.launch {
                kotlinx.coroutines.delay(15000)
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
                windowManager.removeView(view)
                Log.d(tag, "Proverb view removed")
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

    /*
    inner class ScreenReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            Log.d(tag, "ScreenReceiver received action: $action")

            when (action) {
                Intent.ACTION_SCREEN_ON -> {
                    if (displayMode == 1) {
                        Log.d(tag, "Screen on with mode 1, showing proverb")
                        showProverb()
                    }
                }
                Intent.ACTION_USER_PRESENT -> {
                    // This is the key event for unlock
                    Log.d(tag, "User present/unlocked with mode: $displayMode")
                    if (displayMode == 2) {
                        // Explicitly log the intent to show a proverb
                        Log.d(tag, "Display mode is 2, will show proverb on unlock")
                        showProverb()
                    }
                }
                Intent.ACTION_SCREEN_OFF -> {
                    hideProverb()
                }
            }
        }
    }

     */

    inner class ScreenReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            Log.d(tag, "Screen event: $action, display mode: $displayMode")

            when (action) {
                Intent.ACTION_SCREEN_ON -> {
                    if (displayMode == 1) {
                        Log.d(tag, "Screen on with display mode 1, showing proverb")
                        showProverb()
                    }
                }
                Intent.ACTION_USER_PRESENT -> {
                    // CRITICAL FIX: Better handling of unlock event
                    if (displayMode == 2) {
                        Log.d(tag, "User present with display mode 2, showing proverb")
                        // The delay in showProverb is crucial here
                        showProverb()
                    }
                }
                Intent.ACTION_SCREEN_OFF -> {
                    hideProverb()
                }
            }
        }
    }
}