package com.example.quotex

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class QuoteXApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        // Logging setup - using standard Log for now
        android.util.Log.d("QuoteXApplication", "Application initialized")
    }

    // ONLY implement the property, not the method
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    // REMOVE the getWorkManagerConfiguration() method completely - it's causing the error
}