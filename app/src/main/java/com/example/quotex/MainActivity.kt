package com.example.quotex

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.content.SharedPreferences
import android.view.View
import android.view.Window
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.util.Calendar
import androidx.lifecycle.lifecycleScope
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_OVERLAY = 1001
    private val REQUEST_CODE_POST_NOTIFICATIONS = 1002
    private lateinit var prefs: SharedPreferences
    private var displayMode = 0 // 0 = Off, 1 = Screen On, 2 = User Unlock
    private lateinit var quotePager: ViewPager2
    private lateinit var quoteAdapter: QuoteAdapter
    private val quotes = mutableListOf<Quote>()
    private val proverbsMap = mutableMapOf<Int, List<String>>()
    private lateinit var promisesPager: ViewPager2
    private lateinit var promisesAdapter: QuoteAdapter
    private val promises = mutableListOf<Quote>()

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                currentToast("Overlay permission is required for lock screen quotes")
                displayMode = 0
                prefs.edit().putInt("displayMode", displayMode).apply()
                findViewById<Button>(R.id.toggleQuotesButton)?.let { updateButtonText(it) }
            } else {
                currentToast("Overlay permission granted")
                checkAndStartService()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        try {
            setContentView(R.layout.activity_main)
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to inflate layout: ${e.message}", e)
            currentToast("Failed to load UI")
            return
        }
        Log.d("MainActivity", "Layout loaded: R.layout.activity_main")

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val promisesTabLayout = findViewById<TabLayout>(R.id.promisesTabLayout)
        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(topAppBar)

        prefs = getSharedPreferences("QuotePrefs", MODE_PRIVATE)

        val toggleButton: Button? = findViewById(R.id.toggleQuotesButton)
        val displayPromisesButton = findViewById<Button>(R.id.displayPromisesButton)
        quotePager = findViewById(R.id.quotePager)
        promisesPager = findViewById(R.id.promisesPager)

        if (toggleButton == null) {
            Log.e("MainActivity", "Toggle button not found!")
            currentToast("UI initialization failed")
            return
        }

        displayMode = prefs.getInt("displayMode", 0)

        try {
            loadProverbs()
            loadQuotesForCurrentDay()
            loadPromisesForDisplay()

            quoteAdapter = QuoteAdapter(quotes)
            quotePager.adapter = quoteAdapter
            TabLayoutMediator(tabLayout, quotePager) { _, _ -> }.attach()

            promisesAdapter = QuoteAdapter(promises)
            promisesPager.adapter = promisesAdapter
            TabLayoutMediator(promisesTabLayout, promisesPager) { _, _ -> }.attach()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing quotes: ${e.message}", e)
            currentToast("Failed to initialize quotes")
        }

        updateButtonText(toggleButton)
        updateCurrentQuoteDisplay()
        updatePromisesButtonText(displayPromisesButton)

        toggleButton.setOnClickListener {
            toggleDisplayMode()
            updateButtonText(toggleButton)
            updateServiceState()
            updateCurrentQuoteDisplay()
        }

        displayPromisesButton.setOnClickListener {
            togglePromisesDisplay()
            updatePromisesButtonText(displayPromisesButton)
        }

        findViewById<FloatingActionButton>(R.id.fab_add_promise).setOnClickListener {
            val intent = Intent(this, PromisesActivity::class.java)
            startActivity(intent)
        }

        requestPermissions()
        checkAndStartService()
    }

    private fun loadPromisesForDisplay() {
        val prefsPromises = getSharedPreferences("PromisesPrefs", MODE_PRIVATE)
        val promisesJson = prefsPromises.getString("promises", null)

        promises.clear()

        if (!promisesJson.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<Promise>>() {}.type
                val loadedPromises: List<Promise> = Gson().fromJson(promisesJson, type)
                loadedPromises.forEach { promise ->
                    promises.add(Quote(promise.verse, promise.reference))
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to load promises: ${e.message}")
            }
        }

        if (promises.isEmpty()) {
            promises.add(Quote(
                "To know wisdom and instruction, To perceive the words of understanding",
                "PRO 1:2"
            ))
        }

        if (::promisesAdapter.isInitialized) {
            promisesAdapter.updateQuotes(promises)
        }

        val displayPromises = prefs.getBoolean("displayPromises", false)
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.promises_card).visibility =
            if (displayPromises && promises.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun togglePromisesDisplay() {
        val displayPromises = prefs.getBoolean("displayPromises", false)
        val newState = !displayPromises
        prefs.edit().putBoolean("displayPromises", newState).apply()
        updateServiceState()
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.promises_card).visibility =
            if (newState && promises.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun updatePromisesButtonText(button: Button) {
        val displayPromises = prefs.getBoolean("displayPromises", false)
        button.text = if (displayPromises) "Hide Promises" else "Display Promises"
    }

    private fun loadProverbs() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val inputStream = assets.open("proverbs.json")
                val json = inputStream.bufferedReader().use { it.readText() }
                val jsonObject = org.json.JSONObject(json)

                if (!jsonObject.has("chapters")) {
                    Log.e("MainActivity", "Invalid JSON format: 'chapters' array not found")
                    return@launch
                }

                val chapters = jsonObject.getJSONArray("chapters")
                val tempMap = mutableMapOf<Int, List<String>>()

                for (i in 0 until chapters.length()) {
                    val chapterObj = chapters.getJSONObject(i)
                    if (!chapterObj.has("chapter") || !chapterObj.has("verses")) {
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
                    Log.d("MainActivity", "Successfully loaded ${proverbsMap.size} chapters")
                    loadQuotesForCurrentDay()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to load proverbs: ${e.message}")
                withContext(Dispatchers.Main) {
                    proverbsMap.clear()
                    currentToast("Failed to load quotes")
                }
            }
        }
    }

    private fun loadQuotesForCurrentDay() {
        val currentChapter = getCurrentChapter()
        val verses = proverbsMap[currentChapter] ?: emptyList()

        quotes.clear()
        verses.forEachIndexed { index, text ->
            quotes.add(Quote(text, "Proverbs $currentChapter:${index + 1}"))
        }

        if (::quoteAdapter.isInitialized) {
            quoteAdapter.updateQuotes(quotes)
        }
    }

    private fun getCurrentChapter(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.DAY_OF_MONTH).coerceAtMost(31)
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                try {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    overlayPermissionLauncher.launch(intent)
                    return
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error requesting overlay permission: ${e.message}")
                    currentToast("Failed to request overlay permission")
                    displayMode = 0
                    prefs.edit().putInt("displayMode", displayMode).apply()
                    updateButtonText(findViewById(R.id.toggleQuotesButton))
                }
            } else {
                Log.d("MainActivity", "Overlay permission already granted")
                checkAndStartService()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        REQUEST_CODE_POST_NOTIFICATIONS
                    )
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error requesting notification permission: ${e.message}")
                }
            } else {
                Log.d("MainActivity", "Notification permission already granted")
            }
        }
    }

    private fun currentToast(message: String) {
        try {
            android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to show toast: ${e.message}")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS && grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            currentToast("Notification permission is needed for the service to run properly")
        }
    }

    private fun toggleDisplayMode() {
        displayMode = (displayMode + 1) % 3
        prefs.edit().putInt("displayMode", displayMode).apply()
        Log.d("MainActivity", "Display mode toggled to: $displayMode")
        updateServiceState()
    }

    private fun updateButtonText(button: Button) {
        button.text = when (displayMode) {
            0 -> "Enable Lock Screen Quotes"
            1 -> "Display on Screen On"
            2 -> "Display on Unlock"
            else -> "Toggle Lock Screen Quotes"
        }
    }

    private fun updateServiceState() {
        val intent = Intent(this, ProverbService::class.java)
        intent.putExtra("displayMode", displayMode)
        try {
            if (displayMode == 0) {
                stopService(intent)
                currentToast("Lock screen quotes disabled")
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
                currentToast(if (displayMode == 1) "Quotes will appear when screen turns on" else "Quotes will appear on unlock")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error updating service state: ${e.message}")
            currentToast("Failed to update service")
        }
    }

    private fun checkAndStartService() {
        if (displayMode != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Log.w("MainActivity", "Cannot start service: overlay permission not granted")
                currentToast("Overlay permission is required for lock screen quotes")
                return
            }

            try {
                val intent = Intent(this, ProverbService::class.java)
                intent.putExtra("displayMode", displayMode)
                startService(intent)
                Log.d("MainActivity", "Service started with display mode: $displayMode")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error starting service: ${e.message}")
                currentToast("Failed to start service: ${e.message}")
            }
        }

        val toggleButton: Button? = findViewById(R.id.toggleQuotesButton)
        toggleButton?.let { updateButtonText(it) }
    }

    private fun updateCurrentQuoteDisplay() {
        if (!::quotePager.isInitialized || !::quoteAdapter.isInitialized || quotes.isEmpty()) {
            Log.d("MainActivity", "Cannot update quote display - not initialized or empty")
            return
        }

        val currentQuote = prefs.getString("currentQuote", "") ?: ""
        val currentReference = prefs.getString("quoteReference", "") ?: ""

        if (currentQuote.isNotEmpty()) {
            val currentIndex = quotes.indexOfFirst { it.text == currentQuote && it.reference == currentReference }
            if (currentIndex >= 0 && currentIndex < quotes.size) {
                quotePager.setCurrentItem(currentIndex, false)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadPromisesForDisplay()
        loadQuotesForCurrentDay()
    }
}

data class Quote(val text: String, val reference: String)