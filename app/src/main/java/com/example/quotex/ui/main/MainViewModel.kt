// ui/main/MainViewModel.kt
package com.example.quotex.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.quotex.data.repository.UserPreferencesRepository
import com.example.quotex.data.repository.ProverbsRepository
import com.example.quotex.model.Quote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val proverbsRepository: ProverbsRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val proverbsForToday: LiveData<List<Quote>> =
        proverbsRepository.getProverbsForCurrentDay().asLiveData()

    val displayMode: LiveData<Int> = userPreferencesRepository.displayMode.asLiveData()

    val displayPromises: LiveData<Boolean> = userPreferencesRepository.displayPromises.asLiveData()

    // Initialize with default values
    init {
        viewModelScope.launch {
            try {
                // Set default for displayPromises
                val currentDisplayPromises = userPreferencesRepository.displayPromises.first()
                Log.d("MainViewModel", "Current displayPromises value: $currentDisplayPromises")

                // Set displayPromises to true by default if not already set

                    userPreferencesRepository.setDisplayPromises(true)


                // Check the current display mode correctly
                val currentDisplayMode = userPreferencesRepository.displayMode.first()
                Log.d("MainViewModel", "Current displayMode value: $currentDisplayMode")

                // Optionally set a default display mode other than 0 if needed
                // if (currentDisplayMode == 0) {
                //     userPreferencesRepository.setDisplayMode(1)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error setting default preferences", e)
            }
        }
    }

    fun toggleDisplayMode() {
        viewModelScope.launch {
            val currentMode = displayMode.value ?: 0
            val newMode = (currentMode + 1) % 3
            userPreferencesRepository.setDisplayMode(newMode)
        }
    }

    fun toggleDisplayPromises() {
        viewModelScope.launch {
            val currentSetting = displayPromises.value ?: false
            userPreferencesRepository.setDisplayPromises(!currentSetting)
        }
    }

    fun setDisplayMode(mode: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setDisplayMode(mode)
        }
    }

    // Method to force displayPromises to true (useful for troubleshooting)
    fun enablePromisesDisplay() {
        viewModelScope.launch {
            Log.d("MainViewModel", "Explicitly enabling promises display")
            userPreferencesRepository.setDisplayPromises(true)
        }
    }

    // Your refreshQuotes method
    fun refreshQuotes() {
        viewModelScope.launch {
            try {
                Log.d("MainViewModel", "Manually refreshing quotes...")
                val quotes = proverbsRepository.getProverbsForCurrentDay().first()
                Log.d("MainViewModel", "Refreshed ${quotes.size} quotes")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error refreshing quotes: ${e.message}", e)
            }
        }
    }
}