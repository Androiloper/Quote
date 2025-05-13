// ui/main/MainViewModel.kt
package com.example.quotex.ui.main

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.quotex.data.repository.PromisesRepository
import com.example.quotex.data.repository.UserPreferencesRepository
import com.example.quotex.data.repository.ProverbsRepository
import com.example.quotex.model.Promise
import com.example.quotex.model.Quote
import com.example.quotex.service.ProverbService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val proverbsRepository: ProverbsRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    // Add to constructor parameters of MainViewModel
    private val promisesRepository: PromisesRepository,



    ) : ViewModel() {

    // Add this property to fetch promises
    val promises: LiveData<List<Promise>> = promisesRepository.getAllPromises().asLiveData()

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

    // Method to manually refresh promises
    fun refreshPromises() {
        viewModelScope.launch {
            try {
                Log.d("MainViewModel", "Refreshing promises data")
                // This will cause the Flow to be re-collected
                promisesRepository.getAllPromises().first()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error refreshing promises: ${e.message}", e)
            }
        }
    }


    fun forceInitializePromises() {
        viewModelScope.launch {
            try {
                userPreferencesRepository.setDisplayPromises(true)
                Log.d("MainViewModel", "Forced promises display to be enabled")

                // Force add a sample promise if none exist
                val existingPromises = promisesRepository.getAllPromises().first()
                if (existingPromises.isEmpty()) {
                    val samplePromise = Promise(
                        id = System.currentTimeMillis(),
                        title = "Sample Promise",
                        verse = "Trust in the Lord with all your heart and lean not on your own understanding",
                        reference = "Proverbs 3:5"
                    )
                    promisesRepository.addPromise(samplePromise)
                    Log.d("MainViewModel", "Added sample promise")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to force initialize promises", e)
            }
        }
    }

    fun checkAndRestartProverbService(context: Context) {
        viewModelScope.launch {
            val currentMode = displayMode.value ?: 0
            if (currentMode > 0) {
                // Check if service is running
                val serviceRunning = isServiceRunning(context, ProverbService::class.java)
                Log.d("MainViewModel", "Proverb service running: $serviceRunning, mode: $currentMode")

                if (!serviceRunning) {
                    // Service not running but should be, restart it
                    val serviceIntent = Intent(context, ProverbService::class.java)
                    serviceIntent.putExtra("display_mode", currentMode)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                    Log.d("MainViewModel", "Restarted proverb service with mode: $currentMode")
                }
            }
        }
    }

    private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }

}

