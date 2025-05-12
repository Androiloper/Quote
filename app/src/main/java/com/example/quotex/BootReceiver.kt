/*
package com.example.quotex

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Check user preferences before starting service
            val prefs = context.getSharedPreferences("QuotePrefs", Context.MODE_PRIVATE)
            val displayMode = prefs.getInt("displayMode", 0)

            // Only start if user has enabled quotes
            if (displayMode > 0) {
                val serviceIntent = Intent(context, ProverbService::class.java)
                serviceIntent.putExtra("displayMode", displayMode)
                try {
                    context.startService(serviceIntent)
                } catch (e: Exception) {
                    // Log error but don't crash
                }
            }
        }
    }
}


 */

package com.example.quotex

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Boot completed intent received")

            // Check user preferences before starting service
            val prefs = context.getSharedPreferences("QuotePrefs", Context.MODE_PRIVATE)
            val displayMode = prefs.getInt("displayMode", 0)

            // Only start if user has enabled quotes
            if (displayMode > 0) {
                // Check for overlay permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    !Settings.canDrawOverlays(context)) {
                    Log.e("BootReceiver", "Cannot start service: overlay permission not granted")
                    // No notification here as we can't create a channel without an activity context
                    return
                }

                val serviceIntent = Intent(context, ProverbService::class.java)
                serviceIntent.putExtra("displayMode", displayMode)

                try {
                    // Use startForegroundService for Android O+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                    Log.d("BootReceiver", "Service started with display mode: $displayMode")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Failed to start service: ${e.message}")

                    // Schedule a retry
                    Handler(Looper.getMainLooper()).postDelayed({
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(serviceIntent)
                            } else {
                                context.startService(serviceIntent)
                            }
                            Log.d("BootReceiver", "Service started on retry")
                        } catch (e: Exception) {
                            Log.e("BootReceiver", "Failed to start service on retry: ${e.message}")
                        }
                    }, 10000) // Retry after 10 seconds
                }
            }
        }
    }

}
