package com.example.quotex

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

/**
 * Implementation of App Widget functionality.
 * App Widget that displays daily quotes from Proverbs.
 */
class QuoteWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update all widgets
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Called when the first widget is created
        Log.d(TAG, "Quote widget enabled")
    }

    override fun onDisabled(context: Context) {
        // Called when the last widget is disabled
        Log.d(TAG, "Quote widget disabled")
    }

    companion object {
        private const val TAG = "QuoteWidgetProvider"
        // Add caching variables
        private var cachedChapters: JSONArray? = null
        private var lastCacheTime: Long = 0
        private const val CACHE_DURATION = 3600000 // 1 hour in milliseconds

        @SuppressLint("RemoteViewLayout")
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.quote_widget)
            val (quote, reference) = getDailyQuote(context)

            // Update the widget's TextViews
            views.setTextViewText(R.id.widget_proverb_text, quote)
            views.setTextViewText(R.id.widget_proverb_reference, reference)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }


        private fun getDailyQuote(context: Context): Pair<String, String> {
            try {
                val currentTime = System.currentTimeMillis()
                if (cachedChapters == null || currentTime - lastCacheTime > 24 * 60 * 60 * 1000) {
                    val json = context.assets.open("proverbs.json").bufferedReader().use { it.readText() }
                    cachedChapters = JSONObject(json).getJSONArray("chapters")
                    lastCacheTime = currentTime
                }
                val json = context.assets.open("proverbs.json").bufferedReader().use { it.readText() }
                val chapters = JSONObject(json).getJSONArray("chapters")
                val currentChapter = getCurrentChapter()

                for (i in 0 until chapters.length()) {
                    val chapterObj = chapters.getJSONObject(i)
                    val chapter = chapterObj.getInt("chapter")

                    if (chapter == currentChapter) {
                        val versesArray = chapterObj.getJSONArray("verses")
                        val randomIndex = (0 until versesArray.length()).random()
                        val verse = versesArray.getJSONObject(randomIndex).getString("text")
                        return Pair(verse, "Proverbs $chapter:${randomIndex + 1}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load proverb for widget: ${e.message}")
            }

            return Pair("Wisdom is the principal thing.", "Proverbs 4:7") // Fallback quote
        }

        private fun getCurrentChapter(): Int {
            return Calendar.getInstance().get(Calendar.DAY_OF_MONTH).coerceAtMost(31)
        }
    }



    /*
    private fun getDailyQuote(context: Context): Pair<String, String> {
        try {
            // Check if cache is valid
            val currentTime = System.currentTimeMillis()
            if (cachedChapters == null || currentTime - lastCacheTime > CACHE_DURATION) {
                val json = context.assets.open("proverbs.json").bufferedReader().use { it.readText() }
                cachedChapters = JSONObject(json).getJSONArray("chapters")
                lastCacheTime = currentTime
                Log.d(TAG, "Refreshed proverbs cache")
            }

            val currentChapter = getCurrentChapter()

            // Use cached data
            cachedChapters?.let { chapters ->
                for (i in 0 until chapters.length()) {
                    val chapterObj = chapters.getJSONObject(i)
                    val chapter = chapterObj.getInt("chapter")
                    if (chapter == currentChapter) {
                        val versesArray = chapterObj.getJSONArray("verses")
                        val randomIndex = (0 until versesArray.length()).random()
                        val verse = versesArray.getJSONObject(randomIndex).getString("text")
                        return Pair(verse, "Proverbs $chapter:${randomIndex + 1}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load proverb for widget: ${e.message}")
        }

        return Pair("Wisdom is the principal thing.", "Proverbs 4:7") // Fallback quote
    }

     */

    // Fix day-to-chapter mapping
    private fun getCurrentChapter(): Int {
        val calendar = Calendar.getInstance()
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val maxChapters = 31 // Proverbs has 31 chapters

        // For days beyond 31, wrap around
        return if (dayOfMonth <= maxChapters) {
            dayOfMonth
        } else {
            (dayOfMonth % maxChapters) + 1
        }
    }
}
