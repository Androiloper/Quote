package com.example.quotex

import android.app.Application
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.quotex.service.ProverbService
import com.example.quotex.util.WorkScheduler
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class QuoteXApplication : Application(), Configuration.Provider {
    private val TAG = "QuoteXApplication"

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var workScheduler: WorkScheduler

    override fun onCreate() {
        super.onCreate()
        // Logging setup - using Timber for better logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.d("Application initialized")

        // Schedule all work
        workScheduler.scheduleAllWork()

        // Consider starting the service if it was running before app restart
        checkAndRestartService()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    private fun checkAndRestartService() {
        // This is a lightweight check to resume service if app crashes
        // Full checking is done in MainActivity and by the WorkManager
        if (ProverbService.isServiceRunning) {
            Log.d(TAG, "Service was running flag is true, but service might have been killed")

            // The service might have been killed by the system
            try {
                val intent = Intent(this, ProverbService::class.java).apply {
                    action = ProverbService.ACTION_START_SERVICE
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
                Log.d(TAG, "Service restarted from application")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restart service from application: ${e.message}")
                // Reset the flag since we failed to restart
                ProverbService.isServiceRunning = false
            }
        }
    }
}