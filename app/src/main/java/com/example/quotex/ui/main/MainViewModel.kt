// ui/main/MainViewModel.kt
package com.example.quotex.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.quotex.data.repository.UserPreferencesRepository
import com.example.quotex.data.repository.ProverbsRepository
import com.example.quotex.model.Quote
import dagger.hilt.android.lifecycle.HiltViewModel
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

    // Add the missing setDisplayMode method
    fun setDisplayMode(mode: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setDisplayMode(mode)
        }
    }


}