// app/src/main/java/com/example/quotex/service/ProverbService.kt
package com.example.quotex.service

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

    private fun showProverb() {
        if (displayMode == 0 || !Settings.canDrawOverlays(this)) {
            Log.w(tag, "Cannot show proverb: mode off or no overlay permission")
            return
        }

        serviceScope.launch {
            val quote = proverbsRepository.getRandomProverbForCurrentDay()
            if (quote != null) {
                displayQuote(quote)
            }
        }
    }

    private fun displayQuote(quote: Quote) {
        try {
            if (proverbView != null) {
                windowManager.removeView(proverbView)
                proverbView = null
            }

            val inflater = LayoutInflater.from(this)
            proverbView = inflater.inflate(R.layout.quote_item, null)

            val proverbText = proverbView?.findViewById<TextView>(R.id.proverb_text)
            val proverbReference = proverbView?.findViewById<TextView>(R.id.proverb_reference)

            proverbText?.text = quote.text
            proverbReference?.text = quote.reference

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }

            windowManager.addView(proverbView, params)
            Log.d(tag, "Proverb view added to window")

            // Auto-hide after 10 seconds
            serviceScope.launch {
                kotlinx.coroutines.delay(10000)
                hideProverb()
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to add proverb view: ${e.message}", e)
            hideProverb()
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

    inner class ScreenReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(tag, "Screen event received: ${intent?.action}")
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> {
                    if (displayMode == 1) {
                        showProverb()
                    }
                }
                Intent.ACTION_USER_PRESENT -> {
                    Log.d(tag, "Device unlocked, displayMode: $displayMode")
                    if (displayMode == 2) {
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