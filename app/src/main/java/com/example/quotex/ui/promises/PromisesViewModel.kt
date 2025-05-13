package com.example.quotex.ui.promises

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.quotex.data.repository.PromisesRepository
import com.example.quotex.model.Promise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PromisesViewModel @Inject constructor(
    private val promisesRepository: PromisesRepository
) : ViewModel() {

    private val _databaseReady = MutableLiveData<Boolean>(false)
    val databaseReady: LiveData<Boolean> = _databaseReady

    private val _searchQuery = MutableStateFlow("")

    val promises: LiveData<List<Promise>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            promisesRepository.getAllPromises()
                .catch { e ->
                    Log.e("PromisesViewModel", "Error in promises flow: ${e.message}", e)
                    emit(emptyList())
                }
        } else {
            promisesRepository.searchPromises(query)
                .catch { e ->
                    Log.e("PromisesViewModel", "Error in search flow: ${e.message}", e)
                    emit(emptyList())
                }
        }
    }.asLiveData()

    private val _editingPromise = MutableLiveData<Promise?>(null)
    val editingPromise: LiveData<Promise?> = _editingPromise

    init {
        // Force database initialization and add test promise
        viewModelScope.launch {
            try {
                Log.d("PromisesViewModel", "Initializing database and checking for promises...")
                val currentPromises = promisesRepository.getAllPromises().first()
                Log.d("PromisesViewModel", "Found ${currentPromises.size} existing promises")

                // If no promises exist, add sample ones
                if (currentPromises.isEmpty()) {
                    Log.d("PromisesViewModel", "No promises found, adding sample promises...")
                    addSamplePromises()
                }

                _databaseReady.value = true
                Log.d("PromisesViewModel", "Database successfully initialized")
            } catch (e: Exception) {
                Log.e("PromisesViewModel", "Failed to initialize database: ${e.message}", e)
                _databaseReady.value = false

                // Try one more time after a delay
                delay(1000)
                try {
                    addPromise(
                        "Emergency Test Promise",
                        "This is an emergency test promise after database init failure",
                        "Test 1:1"
                    )
                    Log.d("PromisesViewModel", "Emergency promise added after delay")
                    _databaseReady.value = true
                } catch (e2: Exception) {
                    Log.e("PromisesViewModel", "Second attempt also failed: ${e2.message}", e2)
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addPromise(title: String, verse: String, reference: String) {
        if (title.isBlank() || verse.isBlank()) return

        viewModelScope.launch {
            try {
                val promise = Promise(
                    id = System.currentTimeMillis(),
                    title = title,
                    verse = verse,
                    reference = reference
                )
                promisesRepository.addPromise(promise)
                Log.d("PromisesViewModel", "Promise added: $title")
            } catch (e: Exception) {
                Log.e("PromisesViewModel", "Error adding promise: ${e.message}", e)
            }
        }
    }

    fun updatePromise(promise: Promise) {
        viewModelScope.launch {
            try {
                promisesRepository.updatePromise(promise)
                _editingPromise.value = null
                Log.d("PromisesViewModel", "Promise updated: ${promise.title}")
            } catch (e: Exception) {
                Log.e("PromisesViewModel", "Error updating promise: ${e.message}", e)
            }
        }
    }

    fun deletePromise(promise: Promise) {
        viewModelScope.launch {
            try {
                promisesRepository.deletePromise(promise)
                Log.d("PromisesViewModel", "Promise deleted: ${promise.title}")
            } catch (e: Exception) {
                Log.e("PromisesViewModel", "Error deleting promise: ${e.message}", e)
            }
        }
    }

    fun setEditingPromise(promise: Promise?) {
        _editingPromise.value = promise
    }

    // Add sample promises for testing
    fun addSamplePromises() {
        viewModelScope.launch {
            try {
                addPromise(
                    "God's Strength",
                    "I can do all this through him who gives me strength.",
                    "Philippians 4:13"
                )

                addPromise(
                    "Peace of God",
                    "And the peace of God, which transcends all understanding, will guard your hearts and your minds in Christ Jesus.",
                    "Philippians 4:7"
                )

                addPromise(
                    "Trust in the Lord",
                    "Trust in the Lord with all your heart and lean not on your own understanding; in all your ways submit to him, and he will make your paths straight.",
                    "Proverbs 3:5-6"
                )

                Log.d("PromisesViewModel", "Successfully added sample promises")
            } catch (e: Exception) {
                Log.e("PromisesViewModel", "Error adding sample promises: ${e.message}", e)
            }
        }
    }
}