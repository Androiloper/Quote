package com.example.quotex.work

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.quotex.QuoteWidgetProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class UpdateQuoteWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Update any widgets
            val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
            val widgetComponentName = ComponentName(applicationContext, QuoteWidgetProvider::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponentName)

            if (widgetIds.isNotEmpty()) {
                val intent = android.content.Intent(applicationContext, QuoteWidgetProvider::class.java)
                intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
                applicationContext.sendBroadcast(intent)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}