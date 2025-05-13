// ui/main/MainViewModel.kt
package com.example.quotex.ui.main

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import java.lang.Exception

@HiltViewModel
class MainViewModel @Inject constructor(
    private val proverbsRepository: ProverbsRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val promisesRepository: PromisesRepository,
) : ViewModel() {
    private val TAG = "MainViewModel"

    // Add this property to fetch promises
    val promises: LiveData<List<Promise>> = promisesRepository.getAllPromises().asLiveData()

    val proverbsForToday: LiveData<List<Quote>> =
        proverbsRepository.getProverbsForCurrentDay().asLiveData()

    val displayMode: LiveData<Int> = userPreferencesRepository.displayMode.asLiveData()

    val displayPromises: LiveData<Boolean> = userPreferencesRepository.displayPromises.asLiveData()

    // Service status tracking
    private val _serviceRunning = MutableLiveData<Boolean>(false)
    val serviceRunning: LiveData<Boolean> = _serviceRunning

    // Initialize with default values
    init {
        viewModelScope.launch {
            try {
                // Set default for displayPromises
                val currentDisplayPromises = userPreferencesRepository.displayPromises.first()
                Log.d(TAG, "Current displayPromises value: $currentDisplayPromises")

                // Set displayPromises to true by default if not already set
                if (!currentDisplayPromises) {
                    userPreferencesRepository.setDisplayPromises(true)
                }

                // Check the current display mode correctly
                val currentDisplayMode = userPreferencesRepository.displayMode.first()
                Log.d(TAG, "Current displayMode value: $currentDisplayMode")

                // Update service running status
                _serviceRunning.value = ProverbService.isServiceRunning
            } catch (e: Exception) {
                Log.e(TAG, "Error setting default preferences", e)
            }
        }
    }

    fun toggleDisplayMode() {
        viewModelScope.launch {
            val currentMode = displayMode.value ?: 0
            val newMode = (currentMode + 1) % 3
            userPreferencesRepository.setDisplayMode(newMode)

            // Update service based on new mode
            updateServiceState()
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

            // Update service based on new mode
            updateServiceState()
        }
    }

    // Method to force displayPromises to true (useful for troubleshooting)
    fun enablePromisesDisplay() {
        viewModelScope.launch {
            Log.d(TAG, "Explicitly enabling promises display")
            userPreferencesRepository.setDisplayPromises(true)
        }
    }

    // Your refreshQuotes method
    fun refreshQuotes() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Manually refreshing quotes...")
                val quotes = proverbsRepository.getProverbsForCurrentDay().first()
                Log.d(TAG, "Refreshed ${quotes.size} quotes")
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing quotes: ${e.message}", e)
            }
        }
    }

    // Method to manually refresh promises
    fun refreshPromises() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Refreshing promises data")
                // This will cause the Flow to be re-collected
                promisesRepository.getAllPromises().first()
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing promises: ${e.message}", e)
            }
        }
    }

    fun forceInitializePromises() {
        viewModelScope.launch {
            try {
                userPreferencesRepository.setDisplayPromises(true)
                Log.d(TAG, "Forced promises display to be enabled")

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
                    Log.d(TAG, "Added sample promise")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to force initialize promises", e)
            }
        }
    }

    // Improved service management with more reliable detection
    fun checkAndRestartProverbService(context: Context) {
        viewModelScope.launch {
            val currentMode = displayMode.value ?: 0

            // Update internal tracking
            _serviceRunning.value = ProverbService.isServiceRunning

            if (currentMode > 0) {
                // Check if service is running using our static flag
                val serviceRunning = ProverbService.isServiceRunning
                Log.d(TAG, "Proverb service running check: $serviceRunning, mode: $currentMode")

                if (!serviceRunning) {
                    // Service not running but should be, restart it
                    startProverbService(context, currentMode)
                } else {
                    // Service is running, update its display mode
                    updateServiceDisplayMode(context, currentMode)
                }
            } else if (ProverbService.isServiceRunning) {
                // Service running but should be off, stop it
                stopProverbService(context)
            }
        }
    }

    private fun updateServiceState() {
        viewModelScope.launch {
            val mode = userPreferencesRepository.displayMode.first()
            _serviceRunning.value = mode > 0 && ProverbService.isServiceRunning
        }
    }

    private fun startProverbService(context: Context, displayMode: Int) {
        try {
            val serviceIntent = Intent(context, ProverbService::class.java).apply {
                action = ProverbService.ACTION_START_SERVICE
                putExtra(ProverbService.EXTRA_DISPLAY_MODE, displayMode)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            Log.d(TAG, "Started proverb service with mode: $displayMode")
            _serviceRunning.value = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start service", e)
            _serviceRunning.value = false
        }
    }

    private fun stopProverbService(context: Context) {
        try {
            val serviceIntent = Intent(context, ProverbService::class.java).apply {
                action = ProverbService.ACTION_STOP_SERVICE
            }
            context.startService(serviceIntent)
            Log.d(TAG, "Requested service stop")
            _serviceRunning.value = false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop service", e)
        }
    }

    private fun updateServiceDisplayMode(context: Context, mode: Int) {
        try {
            val updateIntent = Intent(context, ProverbService::class.java).apply {
                action = ProverbService.ACTION_UPDATE_DISPLAY_MODE
                putExtra(ProverbService.EXTRA_DISPLAY_MODE, mode)
            }
            context.startService(updateIntent)
            Log.d(TAG, "Updated service display mode to: $mode")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update service mode", e)
        }
    }
}