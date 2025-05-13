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

    init {
        viewModelScope.launch {
            try {

                Log.d("PromisesDebug", "=============================================")
                Log.d("PromisesDebug", "BEGINNING DATABASE DIAGNOSTIC TEST")
                Log.d("PromisesDebug", "=============================================")

                // Test 1: Try to get all promises
                try {

                    val promisesList = promisesRepository.getAllPromises().first()
                    Log.d("PromisesDebug", "TEST 1: Got ${promisesList.size} promises")
                } catch (e: Exception) {
                    Log.e("PromisesDebug", "TEST 1 FAILED: Could not get promises", e)
                }

                // Test 2: Try to add a test promise
                try {
                    val testPromise = Promise(
                        id = System.currentTimeMillis(),
                        title = "Diagnostic Test Promise",
                        verse = "This is a test promise to diagnose database issues",
                        reference = "Debug 1:1"
                    )
                    Log.d("PromisesDebug", "TEST 2: Attempting to add test promise")
                    promisesRepository.addPromise(testPromise)
                    Log.d("PromisesDebug", "TEST 2: Test promise added successfully")
                } catch (e: Exception) {
                    Log.e("PromisesDebug", "TEST 2 FAILED: Could not add test promise", e)
                }

                // Test 3: Check if the test promise was added
                try {
                    delay(500) // Small delay to ensure transaction completes
                    val updatedList = promisesRepository.getAllPromises().first()
                    Log.d("PromisesDebug", "TEST 3: Now have ${updatedList.size} promises")
                    updatedList.forEach { promise ->
                        Log.d("PromisesDebug", "Promise: ${promise.id} - ${promise.title}")
                    }
                } catch (e: Exception) {
                    Log.e("PromisesDebug", "TEST 3 FAILED: Could not verify promise was added", e)
                }

                Log.d("PromisesDebug", "=============================================")
                Log.d("PromisesDebug", "DIAGNOSTIC TEST COMPLETED")
                Log.d("PromisesDebug", "=============================================")
            } catch (e: Exception) {
                Log.e("PromisesDebug", "DIAGNOSTIC TEST FAILED COMPLETELY", e)
            }
        }
    }

    private val _searchQuery = MutableStateFlow("")

    // Add debug logging to trace the promise data flow
    init {
        viewModelScope.launch {
            promisesRepository.getAllPromises()
                .catch { e ->
                    Log.e("PromisesViewModel", "Error loading promises: ${e.message}", e)
                }
                .collect { promises ->
                    Log.d("PromisesViewModel", "Loaded ${promises.size} promises")
                }
        }
    }

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

    // Make sure we load some initial data if the database is empty
    init {
        viewModelScope.launch {
            try {
                val count = promisesRepository.getAllPromises().first().size
                Log.d("PromisesViewModel", "Initial promise count: $count")

                if (count == 0) {
                    Log.d("PromisesViewModel", "No promises found, adding sample promise")
                    // Add a sample promise if none exist
                    addPromise(
                        "Trust in the Lord",
                        "Trust in the Lord with all your heart and lean not on your own understanding; in all your ways submit to him, and he will make your paths straight.",
                        "Proverbs 3:5-6"
                    )
                }
            } catch (e: Exception) {
                Log.e("PromisesViewModel", "Error checking promises: ${e.message}", e)
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

    // Add this method to manually add sample promises for testing
    fun addSamplePromises() {
        viewModelScope.launch {
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
                "Light in Darkness",
                "The light shines in the darkness, and the darkness has not overcome it.",
                "John 1:5"
            )
        }
    }
}