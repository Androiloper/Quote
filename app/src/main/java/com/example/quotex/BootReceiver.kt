// BootReceiver.kt - Updated to use DataStore
package com.example.quotex

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.quotex.service.ProverbService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// Mirror the same DataStore instance used in UserPreferencesRepository
private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class BootReceiver : BroadcastReceiver() {
    private val TAG = "BootReceiver"
    private val DISPLAY_MODE_KEY = intPreferencesKey("display_mode")
    private val DISPLAY_PROMISES_KEY = booleanPreferencesKey("display_promises")

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d(TAG, "Received intent: ${intent?.action}")

        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed intent received")

            // Launch a coroutine to read from DataStore
            scope.launch {
                try {
                    // Read display mode from DataStore using the same key as UserPreferencesRepository
                    val displayMode = context.dataStore.data
                        .catch { e ->
                            Log.e(TAG, "Error reading preferences", e)
                            emit(emptyPreferences())
                        }
                        .map { preferences ->
                            preferences[DISPLAY_MODE_KEY] ?: 0
                        }
                        .first()

                    Log.d(TAG, "Read display mode from DataStore: $displayMode")

                    // Only start if user has enabled quotes
                    if (displayMode > 0) {
                        startProverbService(context, displayMode)
                    } else {
                        Log.d(TAG, "Quote display is disabled (mode: $displayMode), not starting service")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to read preferences from DataStore", e)
                }
            }
        }
    }

    private fun startProverbService(context: Context, displayMode: Int) {
        // Check for overlay permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Settings.canDrawOverlays(context)) {
            Log.e(TAG, "Cannot start service: overlay permission not granted")
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
            Log.d(TAG, "Service started with display mode: $displayMode")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start service", e)
        }
    }
}