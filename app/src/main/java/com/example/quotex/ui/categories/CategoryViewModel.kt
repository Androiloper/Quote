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
import kotlinx.coroutines.launch
import java.util.Date
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

    init {
        loadInitialData()
    }

    /**
     * Load initial data - this will eventually be replaced with actual database queries
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // For now, we'll load sample data
                // In a real implementation, these would come from repositories
                loadCategories()
                loadRecentPromises()
                loadFavoritePromises()
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error loading initial data: ${e.message}", e)
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
                // In a real implementation, this would load from a repository
                val sampleCategories = listOf(
                    Category(id = 1, name = "CAT 1"),
                    Category(id = 2, name = "CAT 2"),
                    Category(id = 3, name = "CAT 4"),
                    Category(id = 4, name = "CAT 5"),
                    Category(id = 5, name = "CAT 7"),
                    Category(id = 6, name = "CAT 8")
                )
                _categories.value = sampleCategories
                Log.d("CategoryViewModel", "Loaded ${sampleCategories.size} categories")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error loading categories: ${e.message}", e)
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
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newId = (_categories.value.maxOfOrNull { it.id } ?: 0) + 1
                val newCategory = Category(id = newId, name = name)
                _categories.value = _categories.value + newCategory
                Log.d("CategoryViewModel", "Created new category: $name with id: $newId")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error creating category: ${e.message}", e)
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
                // Find the category ID for this category name
                val category = _categories.value.find { it.name == categoryName }
                val categoryId = category?.id ?: 0

                // In a real implementation, this would load from a repository
                val sampleTitles = listOf(
                    Title(id = 1, categoryId = 1, name = "TITLE 1"),
                    Title(id = 2, categoryId = 1, name = "TITLE 2"),
                    Title(id = 3, categoryId = 1, name = "TITLE 3"),
                    Title(id = 4, categoryId = 1, name = "TITLE 4"),
                    Title(id = 5, categoryId = 1, name = "TITLE 5"),
                    Title(id = 6, categoryId = 1, name = "TITLE 6"),

                    Title(id = 7, categoryId = 2, name = "TITLE 7"),
                    Title(id = 8, categoryId = 2, name = "TITLE 8")
                )

                // Filter to only the titles for this category
                val filteredTitles = sampleTitles.filter { it.categoryId == categoryId }
                _titles.value = filteredTitles
                _selectedCategory.value = category

                Log.d("CategoryViewModel", "Loaded ${filteredTitles.size} titles for category $categoryName")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error loading titles: ${e.message}", e)
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
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Find the category ID for this category name
                val category = _categories.value.find { it.name == categoryName }
                val categoryId = category?.id ?: 0

                val newId = (_titles.value.maxOfOrNull { it.id } ?: 0) + 1
                val newTitle = Title(id = newId, categoryId = categoryId, name = name)

                // Add the new title to our list
                _titles.value = _titles.value + newTitle
                Log.d("CategoryViewModel", "Created new title: $name for category $categoryName")

                // Automatically select this title
                _selectedTitle.value = newTitle

                // Create a default subtitle for this title
                createSubtitle("DEFAULT", name)
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error creating title: ${e.message}", e)
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
                // Find the title ID for this title name
                val title = _titles.value.find { it.name == titleName }
                val titleId = title?.id ?: 0

                // In a real implementation, this would load from a repository
                val sampleSubtitles = listOf(
                    Subtitle(id = 1, titleId = 1, name = "DEFAULT"),
                    Subtitle(id = 2, titleId = 1, name = "SUB TITLE 1"),
                    Subtitle(id = 3, titleId = 1, name = "SUB TITLE 2"),

                    Subtitle(id = 4, titleId = 2, name = "DEFAULT"),
                    Subtitle(id = 5, titleId = 2, name = "ANOTHER SUB")
                )

                // Filter to only the subtitles for this title
                val filteredSubtitles = sampleSubtitles.filter { it.titleId == titleId }
                _subtitles.value = filteredSubtitles
                _selectedTitle.value = title

                // If we have subtitles, automatically select the first one
                if (filteredSubtitles.isNotEmpty()) {
                    selectSubtitle(filteredSubtitles.first().id)
                } else {
                    _selectedSubtitle.value = null
                    _promises.value = emptyList()
                }

                Log.d("CategoryViewModel", "Loaded ${filteredSubtitles.size} subtitles for title $titleName")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error loading subtitles: ${e.message}", e)
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
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Find the title ID for this title name
                val title = _titles.value.find { it.name == titleName }
                    ?: _selectedTitle.value
                val titleId = title?.id ?: 0

                val newId = (_subtitles.value.maxOfOrNull { it.id } ?: 0) + 1
                val newSubtitle = Subtitle(id = newId, titleId = titleId, name = name)
                _subtitles.value = _subtitles.value + newSubtitle

                // Automatically select this subtitle
                selectSubtitle(newId)

                Log.d("CategoryViewModel", "Created new subtitle: $name for title $titleName")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error creating subtitle: ${e.message}", e)
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
                // In a real implementation, this would load from a repository
                // based on the subtitleId

                // For now, we'll just show some sample promises
                val samplePromises = listOf(
                    Promise(id = 1, title = "PROMISE 1", verse = "Trust in the Lord with all your heart", reference = "Proverbs 3:5"),
                    Promise(id = 2, title = "PROMISE 2", verse = "I can do all things through Christ", reference = "Philippians 4:13"),
                    Promise(id = 3, title = "PROMISE 3", verse = "For God so loved the world", reference = "John 3:16"),
                    Promise(id = 4, title = "PROMISE 4", verse = "The Lord is my shepherd", reference = "Psalm 23:1")
                )

                // Shuffle the promises so it looks like we're loading different ones
                // for different subtitles
                _promises.value = samplePromises.shuffled().take(2)

                Log.d("CategoryViewModel", "Loaded promises for subtitle $subtitleId")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error loading promises: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadRecentPromises() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // In a real implementation, this would load from a repository
                // For now, we'll just use some sample promises
                val samplePromises = listOf(
                    Promise(id = 1, title = "PROMISE 1", verse = "Trust in the Lord with all your heart", reference = "Proverbs 3:5"),
                    Promise(id = 2, title = "PROMISE 2", verse = "I can do all things through Christ", reference = "Philippians 4:13"),
                    Promise(id = 3, title = "PROMISE 3", verse = "For God so loved the world", reference = "John 3:16")
                )
                _recentPromises.value = samplePromises

                Log.d("CategoryViewModel", "Loaded ${samplePromises.size} recent promises")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error loading recent promises: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadFavoritePromises() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // In a real implementation, this would load from a repository
                // For now, we'll just use some sample promises
                val samplePromises = listOf(
                    Promise(id = 2, title = "PROMISE 2", verse = "I can do all things through Christ", reference = "Philippians 4:13"),
                    Promise(id = 4, title = "PROMISE 4", verse = "The Lord is my shepherd", reference = "Psalm 23:1")
                )
                _favoritePromises.value = samplePromises

                Log.d("CategoryViewModel", "Loaded ${samplePromises.size} favorite promises")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error loading favorite promises: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createPromise(subtitleId: Long, title: String, verse: String, reference: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newId = (_promises.value.maxOfOrNull { it.id } ?: 0) + 1
                val newPromise = Promise(id = newId, title = title, verse = verse, reference = reference)

                // Add to promises list
                _promises.value = _promises.value + newPromise

                // Also add to recent promises
                _recentPromises.value = listOf(newPromise) + _recentPromises.value.take(2)

                Log.d("CategoryViewModel", "Created new promise: $title for subtitle $subtitleId")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error creating promise: ${e.message}", e)
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
}