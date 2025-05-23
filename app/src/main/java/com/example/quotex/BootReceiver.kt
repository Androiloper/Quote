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
import com.example.quotex.data.repository.UserPreferencesRepository
import com.example.quotex.service.ProverbService
import com.example.quotex.util.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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
                    // Use the singleton DataStore
                    val dataStore = DataStoreManager.getDataStore(context)

                    // Read display mode from DataStore using the same key as UserPreferencesRepository
                    val displayMode = dataStore.data
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
        // Rest of the method remains unchanged
    }
}