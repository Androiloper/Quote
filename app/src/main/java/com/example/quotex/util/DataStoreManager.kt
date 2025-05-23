package com.example.quotex.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// Application level singleton DataStore
val Context.dataStore by preferencesDataStore(name = "user_preferences")

/**
 * DataStore manager that provides access to the preferences DataStore
 * This ensures only one DataStore instance exists for user preferences
 */
object DataStoreManager {
    fun getDataStore(context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}