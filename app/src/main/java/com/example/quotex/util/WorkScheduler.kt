// app/src/main/java/com/example/quotex/util/WorkScheduler.kt
package com.example.quotex.util

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.quotex.work.ServiceMonitorWorker
import com.example.quotex.work.UpdateQuoteWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "WorkScheduler"

    fun scheduleAllWork() {
        scheduleQuoteUpdates()
        scheduleServiceMonitor()
    }

    fun scheduleQuoteUpdates() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()

        val updateRequest = PeriodicWorkRequestBuilder<UpdateQuoteWorker>(
            1, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "update_quotes",
            ExistingPeriodicWorkPolicy.KEEP,
            updateRequest
        )

        Timber.d("Quote update work scheduled")
    }

    fun scheduleServiceMonitor() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        // Run every 15 minutes to check if service is running
        val monitorRequest = PeriodicWorkRequestBuilder<ServiceMonitorWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "service_monitor",
            ExistingPeriodicWorkPolicy.KEEP,
            monitorRequest
        )

        Timber.d("Service monitor work scheduled")
    }
}