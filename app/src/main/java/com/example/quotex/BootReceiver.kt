// BootReceiver.kt - Non-Hilt version
package com.example.quotex

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.example.quotex.service.ProverbService

// NO @AndroidEntryPoint annotation
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("BootReceiver", "Received intent: ${intent?.action}")

        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Boot completed intent received")

            // Use SharedPreferences directly instead of the repository
            val prefs = context.getSharedPreferences("QuotePrefs", Context.MODE_PRIVATE)
            val displayMode = prefs.getInt("display_mode", 0)

            // Only start if user has enabled quotes
            if (displayMode > 0) {
                // Check for overlay permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    !Settings.canDrawOverlays(context)) {
                    Log.e("BootReceiver", "Cannot start service: overlay permission not granted")
                    return
                }

                val serviceIntent = Intent(context, ProverbService::class.java)
                serviceIntent.putExtra("display_mode", displayMode)

                try {
                    // Use startForegroundService for Android O+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                    Log.d("BootReceiver", "Service started with display mode: $displayMode")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Failed to start service", e)
                }
            }
        }
    }
}