// app/src/main/java/com/example/quotex/ui/categories/CategoryViewModel.kt

package com.example.quotex.ui.categories

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotex.data.repository.PromisesRepository
import com.example.quotex.model.Category
import com.example.quotex.model.Promise
import com.example.quotex.model.Subtitle
import com.example.quotex.model.Title
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing the hierarchical structure of Categories, Titles, Subtitles, and Promises
 */
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val promisesRepository: PromisesRepository
) : ViewModel() {

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Categories
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    // Selected category
    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory

    // Titles for selected category
    private val _titles = MutableStateFlow<List<Title>>(emptyList())
    val titles: StateFlow<List<Title>> = _titles

    // Selected title
    private val _selectedTitle = MutableStateFlow<Title?>(null)
    val selectedTitle: StateFlow<Title?> = _selectedTitle

    // Subtitles for selected title
    private val _subtitles = MutableStateFlow<List<Subtitle>>(emptyList())
    val subtitles: StateFlow<List<Subtitle>> = _subtitles

    // Selected subtitle
    private val _selectedSubtitle = MutableStateFlow<Subtitle?>(null)
    val selectedSubtitle: StateFlow<Subtitle?> = _selectedSubtitle

    // Promises for selected subtitle
    private val _promises = MutableStateFlow<List<Promise>>(emptyList())
    val promises: StateFlow<List<Promise>> = _promises

    // Recent promises
    private val _recentPromises = MutableStateFlow<List<Promise>>(emptyList())
    val recentPromises: StateFlow<List<Promise>> = _recentPromises

    // Favorite promises
    private val _favoritePromises = MutableStateFlow<List<Promise>>(emptyList())
    val favoritePromises: StateFlow<List<Promise>> = _favoritePromises

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadInitialData()
    }

    /**
     * Load initial data from repository
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                loadCategories()
                loadRecentPromises()
                loadFavoritePromises()
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error loading initial data: ${e.message}", e)
                _error.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Category operations
    fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Load from database via repository
                val allPromises = promisesRepository.getAllPromises().first()

                // Extract unique categories from promises
                val uniqueCategories = allPromises
                    .map { it.title }
                    .distinct()
                    .mapIndexed { index, name ->
                        Category(id = index.toLong() + 1, name = name)
                    }
                    .sortedBy { it.name }

                _categories.value = uniqueCategories
                Log.d("CategoryViewModel", "Loaded ${uniqueCategories.size} categories")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error loading categories: ${e.message}", e)
                _error.value = "Failed to load categories: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectCategory(categoryId: Long) {
        val category = _categories.value.find { it.id == categoryId }
        _selectedCategory.value = category
        category?.let { loadTitlesByCategory(it.name) }
    }

    fun createCategory(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Check if category already exists
                if (_categories.value.any { it.name.equals(name, ignoreCase = true) }) {
                    _error.value = "Category '$name' already exists"
                    return@launch
                }

                // Create a new category
                val newId = (_categories.value.maxOfOrNull { it.id } ?: 0) + 1
                val newCategory = Category(id = newId, name = name)
                _categories.value = _categories.value + newCategory

                // Also create a default subtitle
                createTitle(name = "General", categoryName = name)

                Log.d("CategoryViewModel", "Created new category: $name with id: $newId")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error creating category: ${e.message}", e)
                _error.value = "Failed to create category: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCategory(category: Category, newName: String) {
        if (newName.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Check if the new name already exists
                if (_categories.value.any { it.id != category.id && it.name.equals(newName, ignoreCase = true) }) {
                    _error.value = "Category '$newName' already exists"
                    return@launch
                }

                // Update category in the list
                val updatedCategories = _categories.value.map {
                    if (it.id == category.id) it.copy(name = newName) else it
                }
                _categories.value = updatedCategories

                // Update all promises with this category
                // (In a real app, this would update in the database)
                val allPromises = promisesRepository.getAllPromises().first()
                allPromises.filter { it.title == category.name }.forEach { promise ->
                    promisesRepository.updatePromise(promise.copy(title = newName))
                }

                Log.d("CategoryViewModel", "Updated category ${category.id} from '${category.name}' to '$newName'")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error updating category: ${e.message}", e)
                _error.value = "Failed to update category: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Remove category from list
                _categories.value = _categories.value.filter { it.id != category.id }

                // Delete all promises with this category
                val allPromises = promisesRepository.getAllPromises().first()
                allPromises.filter { it.title == category.name }.forEach { promise ->
                    promisesRepository.deletePromise(promise)
                }

                // If this was the selected category, clear selection
                if (_selectedCategory.value?.id == category.id) {
                    _selectedCategory.value = null
                    _titles.value = emptyList()
                }

                Log.d("CategoryViewModel", "Deleted category: ${category.name}")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error deleting category: ${e.message}", e)
                _error.value = "Failed to delete category: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Title operations
    fun loadTitlesByCategory(categoryName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // In a real implementation, this would query titles from a repository
                // based on the category name
                // For this implementation, we'll create a basic set of titles

                val defaultTitles = listOf(
                    Title(id = 1, categoryId = 1, name = "General"),
                    Title(id = 2, categoryId = 1, name = "Scripture")
                )

                _titles.value = defaultTitles
                _selectedCategory.value = _categories.value.find { it.name == categoryName }

                Log.d("CategoryViewModel", "Loaded titles for category $categoryName")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error loading titles: ${e.message}", e)
                _error.value = "Failed to load titles: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectTitle(titleId: Long) {
        val title = _titles.value.find { it.id == titleId }
        _selectedTitle.value = title
        title?.let { loadSubtitlesByTitle(it.name) }
    }

    fun createTitle(name: String, categoryName: String) {
        if (name.isBlank() || categoryName.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Find the category
                val category = _categories.value.find { it.name == categoryName }
                if (category == null) {
                    _error.value = "Category not found: $categoryName"
                    return@launch
                }

                // Check if title already exists in this category
                if (_titles.value.any { it.name.equals(name, ignoreCase = true) }) {
                    _error.value = "Title '$name' already exists in this category"
                    return@launch
                }

                // Create new title
                val newId = (_titles.value.maxOfOrNull { it.id } ?: 0) + 1
                val newTitle = Title(id = newId, categoryId = category.id, name = name)
                _titles.value = _titles.value + newTitle

                // Create a default subtitle for this title
                createSubtitle("DEFAULT", name)

                Log.d("CategoryViewModel", "Created new title: $name for category $categoryName")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error creating title: ${e.message}", e)
                _error.value = "Failed to create title: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTitle(title: Title, newName: String) {
        if (newName.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Check if the new name already exists in this category
                if (_titles.value.any { it.id != title.id && it.name.equals(newName, ignoreCase = true) }) {
                    _error.value = "Title '$newName' already exists in this category"
                    return@launch
                }

                // Update title in the list
                val updatedTitles = _titles.value.map {
                    if (it.id == title.id) it.copy(name = newName) else it
                }
                _titles.value = updatedTitles

                // If this was the selected title, update selection
                if (_selectedTitle.value?.id == title.id) {
                    _selectedTitle.value = updatedTitles.find { it.id == title.id }
                }

                Log.d("CategoryViewModel", "Updated title ${title.id} from '${title.name}' to '$newName'")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error updating title: ${e.message}", e)
                _error.value = "Failed to update title: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTitle(title: Title) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Remove title from list
                _titles.value = _titles.value.filter { it.id != title.id }

                // If this was the selected title, clear selection
                if (_selectedTitle.value?.id == title.id) {
                    _selectedTitle.value = null
                    _subtitles.value = emptyList()
                    _promises.value = emptyList()
                }

                Log.d("CategoryViewModel", "Deleted title: ${title.name}")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error deleting title: ${e.message}", e)
                _error.value = "Failed to delete title: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Subtitle operations
    fun loadSubtitlesByTitle(titleName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // In a real implementation, this would query subtitles from a repository
                // For this implementation, we'll create a basic set of subtitles

                val defaultSubtitles = listOf(
                    Subtitle(id = 1, titleId = 1, name = "DEFAULT")
                )

                // Find the title ID
                val title = _titles.value.find { it.name == titleName }

                _subtitles.value = defaultSubtitles
                _selectedTitle.value = title

                // If we have subtitles, automatically select the first one
                if (defaultSubtitles.isNotEmpty()) {
                    selectSubtitle(defaultSubtitles.first().id)
                } else {
                    _selectedSubtitle.value = null
                    _promises.value = emptyList()
                }

                Log.d("CategoryViewModel", "Loaded subtitles for title $titleName")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error loading subtitles: ${e.message}", e)
                _error.value = "Failed to load subtitles: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectSubtitle(subtitleId: Long) {
        val subtitle = _subtitles.value.find { it.id == subtitleId }
        _selectedSubtitle.value = subtitle
        loadPromisesForSubtitle(subtitleId)
    }

    fun createSubtitle(name: String, titleName: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Find the title
                val title = _titles.value.find { it.name == titleName } ?: _selectedTitle.value
                if (title == null) {
                    _error.value = "Title not found: $titleName"
                    return@launch
                }

                // Check if subtitle already exists for this title
                if (_subtitles.value.any { it.name.equals(name, ignoreCase = true) }) {
                    _error.value = "Subtitle '$name' already exists for this title"
                    return@launch
                }

                // Create new subtitle
                val newId = (_subtitles.value.maxOfOrNull { it.id } ?: 0) + 1
                val newSubtitle = Subtitle(id = newId, titleId = title.id, name = name)
                _subtitles.value = _subtitles.value + newSubtitle

                // Automatically select this subtitle
                selectSubtitle(newId)

                Log.d("CategoryViewModel", "Created new subtitle: $name for title $titleName")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error creating subtitle: ${e.message}", e)
                _error.value = "Failed to create subtitle: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSubtitle(subtitle: Subtitle, newName: String) {
        if (newName.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Check if the new name already exists for this title
                if (_subtitles.value.any { it.id != subtitle.id && it.name.equals(newName, ignoreCase = true) }) {
                    _error.value = "Subtitle '$newName' already exists for this title"
                    return@launch
                }

                // Update subtitle in the list
                val updatedSubtitles = _subtitles.value.map {
                    if (it.id == subtitle.id) it.copy(name = newName) else it
                }
                _subtitles.value = updatedSubtitles

                // If this was the selected subtitle, update selection
                if (_selectedSubtitle.value?.id == subtitle.id) {
                    _selectedSubtitle.value = updatedSubtitles.find { it.id == subtitle.id }
                }

                Log.d("CategoryViewModel", "Updated subtitle ${subtitle.id} from '${subtitle.name}' to '$newName'")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error updating subtitle: ${e.message}", e)
                _error.value = "Failed to update subtitle: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSubtitle(subtitle: Subtitle) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Can't delete the last subtitle
                if (_subtitles.value.size <= 1) {
                    _error.value = "Cannot delete the last subtitle"
                    return@launch
                }

                // Remove subtitle from list
                _subtitles.value = _subtitles.value.filter { it.id != subtitle.id }

                // If this was the selected subtitle, select another one
                if (_selectedSubtitle.value?.id == subtitle.id) {
                    val newSubtitle = _subtitles.value.firstOrNull()
                    if (newSubtitle != null) {
                        selectSubtitle(newSubtitle.id)
                    } else {
                        _selectedSubtitle.value = null
                        _promises.value = emptyList()
                    }
                }

                Log.d("CategoryViewModel", "Deleted subtitle: ${subtitle.name}")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error deleting subtitle: ${e.message}", e)
                _error.value = "Failed to delete subtitle: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Promise operations
    private fun loadPromisesForSubtitle(subtitleId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // In a real implementation, this would query promises from the repository
                // For now, let's just load all promises for the current category

                val selectedCategory = _selectedCategory.value
                if (selectedCategory != null) {
                    val allPromises = promisesRepository.getAllPromises().first()
                    val filteredPromises = allPromises
                        .filter { it.title == selectedCategory.name }
                        .take(5) // Limit to 5 promises for UI display

                    _promises.value = filteredPromises
                } else {
                    _promises.value = emptyList()
                }

                Log.d("CategoryViewModel", "Loaded promises for subtitle $subtitleId")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error loading promises: ${e.message}", e)
                _error.value = "Failed to load promises: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadRecentPromises() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get the most recent promises based on ID (assuming higher ID = more recent)
                val allPromises = promisesRepository.getAllPromises().first()
                _recentPromises.value = allPromises
                    .sortedByDescending { it.id }
                    .take(5) // Limit to 5 most recent promises

                Log.d("CategoryViewModel", "Loaded ${_recentPromises.value.size} recent promises")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error loading recent promises: ${e.message}", e)
                _error.value = "Failed to load recent promises: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadFavoritePromises() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // In a real app, you would have a "favorite" flag to filter by
                // For this implementation, just use a subset of all promises

                val allPromises = promisesRepository.getAllPromises().first()
                _favoritePromises.value = allPromises
                    .take(3) // Just take the first 3 as "favorites"

                Log.d("CategoryViewModel", "Loaded ${_favoritePromises.value.size} favorite promises")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error loading favorite promises: ${e.message}", e)
                _error.value = "Failed to load favorite promises: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createPromise(subtitleId: Long, title: String, verse: String, reference: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newPromise = Promise(
                    id = System.currentTimeMillis(),
                    title = title,
                    verse = verse,
                    reference = reference
                )

                // Save to repository
                promisesRepository.addPromise(newPromise)

                // Update local lists
                _promises.value = _promises.value + newPromise
                _recentPromises.value = listOf(newPromise) + _recentPromises.value.take(4)

                Log.d("CategoryViewModel", "Created new promise: $title for subtitle $subtitleId")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error creating promise: ${e.message}", e)
                _error.value = "Failed to create promise: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavoritePromise(promiseId: Long) {
        viewModelScope.launch {
            try {
                val promise = _promises.value.find { it.id == promiseId }
                    ?: _recentPromises.value.find { it.id == promiseId }

                promise?.let {
                    if (_favoritePromises.value.any { fav -> fav.id == promiseId }) {
                        // Remove from favorites
                        _favoritePromises.value = _favoritePromises.value.filter { fav -> fav.id != promiseId }
                    } else {
                        // Add to favorites
                        _favoritePromises.value = listOf(it) + _favoritePromises.value
                    }
                }
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error toggling favorite: ${e.message}", e)
            }
        }
    }

    // Clear error message
    fun clearError() {
        _error.value = null
    }
}