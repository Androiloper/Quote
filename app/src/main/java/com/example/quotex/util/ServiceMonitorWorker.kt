// app/src/main/java/com/example/quotex/work/ServiceMonitorWorker.kt
package com.example.quotex.work

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.quotex.service.ProverbService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

/**
 * Worker to periodically check if the ProverbService is running
 * and restart it if necessary
 */
@HiltWorker
class ServiceMonitorWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    private val TAG = "ServiceMonitorWorker"
    private val DISPLAY_MODE_KEY = intPreferencesKey("display_mode")

    override suspend fun doWork(): Result {
        Log.d(TAG, "Service monitor worker running")

        try {
            // Read display mode from DataStore
            val displayMode = applicationContext.dataStore.data
                .catch { e ->
                    Log.e(TAG, "Error reading preferences", e)
                    emit(emptyPreferences())
                }
                .map { preferences ->
                    preferences[DISPLAY_MODE_KEY] ?: 0
                }
                .first()

            Log.d(TAG, "Current display mode: $displayMode")

            // Only restart if display mode is enabled (1 or 2)
            if (displayMode > 0) {
                if (!ProverbService.isServiceRunning) {
                    Log.d(TAG, "Service not running but should be, restarting")
                    startProverbService(displayMode)
                } else {
                    Log.d(TAG, "Service is already running with mode: $displayMode")
                }
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in service monitor worker: ${e.message}", e)
            return Result.retry()
        }
    }

    private fun startProverbService(mode: Int) {
        try {
            val serviceIntent = Intent(applicationContext, ProverbService::class.java).apply {
                action = ProverbService.ACTION_START_SERVICE
                putExtra(ProverbService.EXTRA_DISPLAY_MODE, mode)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                applicationContext.startForegroundService(serviceIntent)
            } else {
                applicationContext.startService(serviceIntent)
            }
            Log.d(TAG, "Started service with mode: $mode")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start service: ${e.message}", e)
        }
    }
}