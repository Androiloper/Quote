package com.example.quotex

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
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Calendar

class ProverbService : Service() {

    companion object {
        const val channelId = "quotex_channel"
        const val notificationId = 1
        const val tag = "ProverbService"
    }

    private lateinit var windowManager: WindowManager
    private var proverbView: View? = null
    private val proverbsMap = mutableMapOf<Int, List<String>>()
    private var isDataLoaded = false
    private var displayMode = 0 // 0 = Off, 1 = Screen On, 2 = Unlock
    private val serviceScope = CoroutineScope(Dispatchers.Main)
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
            loadProverbs()
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
    }

    override fun onBind(intent: Intent?) = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Quotes",
                android.app.NotificationManager.IMPORTANCE_LOW
            ).apply { setShowBadge(false) }
            getSystemService(android.app.NotificationManager::class.java).createNotificationChannel(channel)
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

    private fun loadProverbs() {
        serviceScope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    assets.open("proverbs.json").bufferedReader().use { it.readText() }
                }
                val chapters = JSONObject(json).getJSONArray("chapters")
                val tempMap = mutableMapOf<Int, List<String>>()
                for (i in 0 until chapters.length()) {
                    val chapterObj = chapters.getJSONObject(i)
                    if (!chapterObj.has("chapter") || !chapterObj.has("verses")) {
                        Log.w(tag, "Skipping invalid chapter object at index $i")
                        continue
                    }
                    val chapter = chapterObj.getInt("chapter")
                    val versesArray = chapterObj.getJSONArray("verses")
                    val verses = mutableListOf<String>()
                    for (j in 0 until versesArray.length()) {
                        val verseObj = versesArray.getJSONObject(j)
                        if (verseObj.has("text")) {
                            verses.add(verseObj.getString("text"))
                        }
                    }
                    if (verses.isNotEmpty()) {
                        tempMap[chapter] = verses
                    }
                }
                withContext(Dispatchers.Main) {
                    proverbsMap.clear()
                    proverbsMap.putAll(tempMap)
                    isDataLoaded = true
                    Log.d(tag, "Successfully loaded ${proverbsMap.size} chapters")
                    if (displayMode != 0 && isScreenOn()) {
                        showProverb()
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to load proverbs: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    isDataLoaded = false
                }
            }
        }
    }

    private fun showProverb() {
        if (!isDataLoaded || displayMode == 0 || !Settings.canDrawOverlays(this)) {
            Log.w(tag, "Cannot show proverb: data not loaded, mode off, or no overlay permission")
            return
        }

        val currentChapter = Calendar.getInstance().get(Calendar.DAY_OF_MONTH).coerceAtMost(31)
        val verses = proverbsMap[currentChapter] ?: emptyList()
        if (verses.isEmpty()) {
            Log.w(tag, "No verses available for chapter $currentChapter")
            return
        }

        val randomVerse = verses.random()
        val inflater = LayoutInflater.from(this)
        proverbView = inflater.inflate(R.layout.quote_item, null)

        val proverbText = proverbView?.findViewById<TextView>(R.id.quote_text)
        val proverbReference = proverbView?.findViewById<TextView>(R.id.quote_reference)
        proverbText?.text = randomVerse
        proverbReference?.text = "Proverbs $currentChapter:${verses.indexOf(randomVerse) + 1}"

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

        try {
            windowManager.addView(proverbView, params)
            Log.d(tag, "Proverb view added to window")
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
                    if (displayMode == 1 && isDataLoaded) {
                        showProverb()
                    }
                }
                Intent.ACTION_USER_PRESENT -> {
                    Log.d(tag, "Device unlocked, displayMode: $displayMode")
                    if (displayMode == 2 && isDataLoaded) {
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