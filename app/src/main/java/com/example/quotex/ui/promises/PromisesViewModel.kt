package com.example.quotex.ui.promises

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.quotex.data.repository.PromisesRepository
import com.example.quotex.model.Promise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PromisesViewModel @Inject constructor(
    private val promisesRepository: PromisesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val promises: LiveData<List<Promise>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            promisesRepository.getAllPromises()
        } else {
            promisesRepository.searchPromises(query)
        }
    }.asLiveData()

    private val _editingPromise = MutableLiveData<Promise?>(null)
    val editingPromise: LiveData<Promise?> = _editingPromise

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addPromise(title: String, verse: String, reference: String) {
        if (title.isBlank() || verse.isBlank()) return

        viewModelScope.launch {
            val promise = Promise(
                title = title,
                verse = verse,
                reference = reference
            )
            promisesRepository.addPromise(promise)
        }
    }

    fun updatePromise(promise: Promise) {
        viewModelScope.launch {
            promisesRepository.updatePromise(promise)
            _editingPromise.value = null
        }
    }

    fun deletePromise(promise: Promise) {
        viewModelScope.launch {
            promisesRepository.deletePromise(promise)
        }
    }

    fun setEditingPromise(promise: Promise?) {
        _editingPromise.value = promise
    }
}