package com.example.quotex.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    val displayMode: Flow<Int> = dataStore.data
        .catch { e ->
            Log.e("UserPreferences", "Error reading displayMode", e)
            emit(emptyPreferences())
        }
        .map { preferences ->
            preferences[DISPLAY_MODE_KEY] ?: 0
        }

    // Fix: Use actual dataStore instead of hardcoded flow
    val displayPromises: Flow<Boolean> = dataStore.data
        .catch { e ->
            Log.e("UserPreferences", "Error reading displayPromises", e)
            emit(emptyPreferences())
        }
        .map { preferences ->
            // Default to true to ensure promises feature is on
            preferences[DISPLAY_PROMISES_KEY] ?: true
        }

    suspend fun setDisplayMode(mode: Int) {
        try {
            dataStore.edit { preferences ->
                preferences[DISPLAY_MODE_KEY] = mode
            }
            Log.d("UserPreferences", "Display mode set to: $mode")
        } catch (e: Exception) {
            Log.e("UserPreferences", "Error setting display mode", e)
        }
    }

    suspend fun setDisplayPromises(display: Boolean) {
        try {
            dataStore.edit { preferences ->
                preferences[DISPLAY_PROMISES_KEY] = display
            }
            Log.d("UserPreferences", "Display promises set to: $display")
        } catch (e: Exception) {
            Log.e("UserPreferences", "Error setting display promises", e)
        }
    }

    companion object {
        private val DISPLAY_MODE_KEY = intPreferencesKey("display_mode")
        private val DISPLAY_PROMISES_KEY = booleanPreferencesKey("display_promises")
    }
}