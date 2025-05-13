package com.example.quotex.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    val displayMode: Flow<Int> = dataStore.data.map { preferences ->
        preferences[DISPLAY_MODE_KEY] ?: 0
    }

    val displayPromises: Flow<Boolean> = flow { emit(true) }

    suspend fun setDisplayMode(mode: Int) {
        dataStore.edit { preferences ->
            preferences[DISPLAY_MODE_KEY] = mode
        }
    }

    suspend fun setDisplayPromises(display: Boolean) {
        dataStore.edit { preferences ->
            preferences[DISPLAY_PROMISES_KEY] = display
        }
    }

    companion object {
        private val DISPLAY_MODE_KEY = intPreferencesKey("display_mode")
        private val DISPLAY_PROMISES_KEY = booleanPreferencesKey("display_promises")
    }
}