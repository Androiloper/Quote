package com.example.quotex.ui.categories

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotex.data.db.entities.PromiseEntity
import com.example.quotex.data.repository.PromisesRepository
import com.example.quotex.model.Category
import com.example.quotex.model.Promise
import com.example.quotex.model.Subtitle
import com.example.quotex.model.Title
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
                // Using promisesRepository.getCategories() directly
                _categories.value = promisesRepository.getCategories().first()
                Log.d("CategoryViewModel", "Loaded ${_categories.value.size} categories via repository")

                // Load recent and favorite promises using repository methods
                _recentPromises.value = promisesRepository.getRecentPromises().first()
                Log.d("CategoryViewModel", "Loaded ${_recentPromises.value.size} recent promises")

                _favoritePromises.value = promisesRepository.getFavoritePromises().first()
                Log.d("CategoryViewModel", "Loaded ${_favoritePromises.value.size} favorite promises")

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
                _categories.value = promisesRepository.getCategories().first()
                Log.d("CategoryViewModel", "Loaded ${_categories.value.size} categories")
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
        // Reset downstream selections
        _titles.value = emptyList()
        _selectedTitle.value = null
        _subtitles.value = emptyList()
        _selectedSubtitle.value = null
        _promises.value = emptyList()
        category?.let { loadTitlesByCategory(it.name) }
    }

    fun createCategory(name: String) {
        if (name.isBlank()) {
            _error.value = "Category name cannot be empty."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (_categories.value.any { it.name.equals(name, ignoreCase = true) }) {
                    _error.value = "Category '$name' already exists."
                    return@launch
                }

                // This is a view model-level "creation".
                // In a real app, repository.createCategory(name) would persist it and return the new Category.
                // For now, we simulate it. The ID generation here is purely for the ViewModel state.
                val newId = (_categories.value.maxOfOrNull { it.id } ?: 0L) + 1L
                val newCategory = Category(id = newId, name = name)
                _categories.value = (_categories.value + newCategory).sortedBy { it.name }
                _selectedCategory.value = newCategory // Auto-select new category

                // Create a default title and subtitle for the new category
                // This part interacts with how titles/subtitles are "created" if they don't exist in promises yet.
                // Let's assume createTitle will handle adding it to _titles and selecting it.
                createTitle(name = "General", categoryName = newCategory.name)

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
        if (newName.isBlank()) {
            _error.value = "New category name cannot be empty."
            return
        }
        if (category.name.equals(newName, ignoreCase = true)) {
            // No change
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (_categories.value.any { it.id != category.id && it.name.equals(newName, ignoreCase = true) }) {
                    _error.value = "Another category with name '$newName' already exists."
                    return@launch
                }

                // Call repository to rename
                promisesRepository.renameCategory(category.name, newName)

                // Refresh categories from repository
                loadCategories() // This will fetch the updated list

                // If the updated category was selected, update its name in selection
                if (_selectedCategory.value?.id == category.id) {
                    _selectedCategory.value = _selectedCategory.value?.copy(name = newName)
                    // Potentially reload titles if category name change affects fetching logic that depends on the name
                    _selectedCategory.value?.let { loadTitlesByCategory(it.name) }
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
                promisesRepository.deleteCategory(category.name)
                // Refresh categories
                loadCategories()

                if (_selectedCategory.value?.id == category.id) {
                    _selectedCategory.value = null
                    _titles.value = emptyList()
                    _selectedTitle.value = null
                    _subtitles.value = emptyList()
                    _selectedSubtitle.value = null
                    _promises.value = emptyList()
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
                val fetchedTitles = promisesRepository.getTitlesByCategory(categoryName).first()
                _titles.value = fetchedTitles
                _selectedCategory.value = _categories.value.find { it.name == categoryName }

                // Reset downstream selections
                _selectedTitle.value = null
                _subtitles.value = emptyList()
                _selectedSubtitle.value = null
                _promises.value = emptyList()

                Log.d("CategoryViewModel", "Loaded ${fetchedTitles.size} titles for category $categoryName")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error loading titles for $categoryName: ${e.message}", e)
                _error.value = "Failed to load titles: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectTitle(titleId: Long) {
        val title = _titles.value.find { it.id == titleId }
        _selectedTitle.value = title
        // Reset downstream selections
        _subtitles.value = emptyList()
        _selectedSubtitle.value = null
        _promises.value = emptyList()
        title?.let {
            _selectedCategory.value?.name?.let { categoryName ->
                loadSubtitlesByTitle(categoryName, it.name)
            } ?: run {
                Log.e("CategoryViewModel", "Cannot select title, category not selected.")
                _error.value = "Category not selected."
            }
        }
    }

    // In CategoryViewModel.kt
    // In CategoryViewModel.kt
    fun createTitle(name: String, categoryName: String) {
        if (name.isBlank()) {
            _error.value = "Title name cannot be empty."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val category = _categories.value.find { it.name == categoryName }
                if (category == null) {
                    _error.value = "Category '$categoryName' not found for creating title."
                    return@launch
                }

                // Check if title already exists
                if (_titles.value.any { it.name.equals(name, ignoreCase = true) }) {
                    _error.value = "Title '$name' already exists in category '$categoryName'."
                    return@launch
                }

                // CRITICAL: Match database format exactly based on the sample data
                // title field = category name directly (not "CategoryName:Title")
                // reference field = "TitleName|SubtitleName|ScriptureReference"
                val initialPromise = Promise(
                    id = System.currentTimeMillis(),
                    // FIXED FORMAT: "CategoryName:Title Entry" instead of just CategoryName
                    title = "$categoryName${PromisesRepository.CATEGORY_SEPARATOR}Title Entry",
                    verse = "This is a placeholder for title '$name'",
                    // Format: "TitleName|SubtitleName"
                    reference = "$name${PromisesRepository.TITLE_SEPARATOR}General"
                )

                // Save to database
                promisesRepository.addPromise(initialPromise)

                // Force refresh titles after database save
                delay(300) // Ensure database operation completes
                loadTitlesByCategory(categoryName)

                Log.d("CategoryViewModel", "Created title: $name with format: title=$categoryName, reference=$name|General Reference")

            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error creating title: ${e.message}", e)
                _error.value = "Failed to create title: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTitle(title: Title, newName: String) {
        if (newName.isBlank()) {
            _error.value = "New title name cannot be empty."
            return
        }
        val category = _selectedCategory.value ?: run {
            _error.value = "No category selected to update title."
            return
        }
        if (title.name.equals(newName, ignoreCase = true)) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (_titles.value.any { it.id != title.id && it.categoryId == title.categoryId && it.name.equals(newName, ignoreCase = true) }) {
                    _error.value = "Another title with name '$newName' already exists in this category."
                    return@launch
                }
                promisesRepository.renameTitle(category.name, title.name, newName)
                loadTitlesByCategory(category.name) // Refresh titles

                if (_selectedTitle.value?.id == title.id) {
                    _selectedTitle.value = _selectedTitle.value?.copy(name = newName)
                    _selectedTitle.value?.let {  loadSubtitlesByTitle(category.name, it.name) }
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
        val category = _selectedCategory.value ?: run {
            _error.value = "No category selected to delete title."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                promisesRepository.deleteTitle(category.name, title.name)
                loadTitlesByCategory(category.name) // Refresh titles

                if (_selectedTitle.value?.id == title.id) {
                    _selectedTitle.value = null
                    _subtitles.value = emptyList()
                    _selectedSubtitle.value = null
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
    // Modified to take categoryName as well, as it's needed for repository calls
    fun loadSubtitlesByTitle(categoryName: String, titleName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val fetchedSubtitles = promisesRepository.getSubtitlesByTitle(categoryName, titleName).first()
                _subtitles.value = fetchedSubtitles
                // Ensure selectedTitle reflects the title for which subtitles are loaded
                _selectedTitle.value = _titles.value.find { it.name == titleName && it.categoryId == _selectedCategory.value?.id }

                // Reset downstream selections
                _selectedSubtitle.value = null
                _promises.value = emptyList()

                // If we have subtitles, automatically select the first one (optional)
                if (fetchedSubtitles.isNotEmpty()) {
                    // selectSubtitle(fetchedSubtitles.first().id) // Auto-select first subtitle
                }

                Log.d("CategoryViewModel", "Loaded ${fetchedSubtitles.size} subtitles for title '$titleName' in category '$categoryName'")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error loading subtitles for $titleName: ${e.message}", e)
                _error.value = "Failed to load subtitles: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectSubtitle(subtitleId: Long) {
        val subtitle = _subtitles.value.find { it.id == subtitleId }
        _selectedSubtitle.value = subtitle
        _promises.value = emptyList() // Clear old promises
        subtitle?.let {
            // Call the updated loadPromisesForSubtitle
            loadPromisesForSubtitle()
        }
    }

    fun createSubtitle(name: String, titleName: String, categoryName: String) {
        if (name.isBlank()) {
            _error.value = "Subtitle name cannot be empty."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val title = _titles.value.find { it.name == titleName }
                if (title == null) {
                    _error.value = "Title '$titleName' not found for creating subtitle."
                    return@launch
                }

                // Create actual database entry for subtitle
                val subtitlePromise = Promise(
                    id = System.currentTimeMillis(),
                    title = "$categoryName${PromisesRepository.CATEGORY_SEPARATOR}Subtitle Entry",
                    verse = "This is a placeholder for subtitle '$name'",
                    // Format MUST match what repository extraction expects
                    reference = "$titleName${PromisesRepository.TITLE_SEPARATOR}$name"
                )

                // Save to database
                val promiseId = promisesRepository.addPromise(subtitlePromise)

                // Important: Refresh subtitles from database
                delay(100)
                loadSubtitlesByTitle(categoryName, titleName)

                Log.d("CategoryViewModel", "Created subtitle entry: $name under $titleName with ID $promiseId")

                // Update VM state for immediate UI feedback
                val newId = (_subtitles.value.maxOfOrNull { it.id } ?: 0L) + 1L
                val newSubtitle = Subtitle(id = newId, titleId = title.id, name = name)
                _subtitles.value = (_subtitles.value + newSubtitle).sortedBy { it.name }
                _selectedSubtitle.value = newSubtitle

            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error creating subtitle: ${e.message}", e)
                _error.value = "Failed to create subtitle: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSubtitle(subtitle: Subtitle, newName: String) {
        if (newName.isBlank()) {
            _error.value = "New subtitle name cannot be empty."
            return
        }
        val category = _selectedCategory.value ?: run { _error.value = "Category not selected."; return }
        val title = _selectedTitle.value ?: run { _error.value = "Title not selected."; return }
        if (subtitle.name.equals(newName, ignoreCase = true)) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (_subtitles.value.any { it.id != subtitle.id && it.titleId == subtitle.titleId && it.name.equals(newName, ignoreCase = true) }) {
                    _error.value = "Another subtitle '$newName' already exists for this title."
                    return@launch
                }
                promisesRepository.renameSubtitle(category.name, title.name, subtitle.name, newName)
                loadSubtitlesByTitle(category.name, title.name) // Refresh

                if (_selectedSubtitle.value?.id == subtitle.id) {
                    _selectedSubtitle.value = _selectedSubtitle.value?.copy(name = newName)
                    loadPromisesForSubtitle() // Reload promises for renamed subtitle
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
        val category = _selectedCategory.value ?: run { _error.value = "Category not selected."; return }
        val title = _selectedTitle.value ?: run { _error.value = "Title not selected."; return }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Optional: Prevent deleting the last subtitle if desired
                // if (_subtitles.value.size <= 1) {
                // _error.value = "Cannot delete the last subtitle."
                // return@launch
                // }
                promisesRepository.deleteSubtitle(category.name, title.name, subtitle.name)
                loadSubtitlesByTitle(category.name, title.name) // Refresh

                if (_selectedSubtitle.value?.id == subtitle.id) {
                    _selectedSubtitle.value = null // Clear selection
                    _promises.value = emptyList()
                    // Optionally select another subtitle if available
                    // _subtitles.value.firstOrNull()?.let { selectSubtitle(it.id) }
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
    // Modified to not take subtitleId as it relies on _selectedSubtitle.value
    private fun loadPromisesForSubtitle() {
        viewModelScope.launch {
            val currentCategory = _selectedCategory.value
            val currentTitle = _selectedTitle.value
            val currentSubtitle = _selectedSubtitle.value

            if (currentCategory != null && currentTitle != null && currentSubtitle != null) {
                _isLoading.value = true
                try {
                    _promises.value = promisesRepository.getPromisesByHierarchy(
                        categoryName = currentCategory.name,
                        titleName = currentTitle.name,
                        subtitleName = currentSubtitle.name
                    ).first() // Collect the first emission from the Flow

                    Log.d("CategoryViewModel", "Loaded ${_promises.value.size} promises for ${currentCategory.name} > ${currentTitle.name} > ${currentSubtitle.name}")
                } catch (e: Exception) {
                    Log.e("CategoryViewModel", "Error loading promises for subtitle ${currentSubtitle.name}: ${e.message}", e)
                    _error.value = "Failed to load promises: ${e.message}"
                    _promises.value = emptyList()
                } finally {
                    _isLoading.value = false
                }
            } else {
                _promises.value = emptyList() // Clear promises if hierarchy is not fully selected
                Log.w("CategoryViewModel", "Cannot load promises: Category, Title, or Subtitle not fully selected.")
                // Optionally set an error message if this state is unexpected
                // _error.value = "Select a category, title, and subtitle to view promises."
            }
        }
    }


    fun loadRecentPromises() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _recentPromises.value = promisesRepository.getRecentPromises().first()
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
                _favoritePromises.value = promisesRepository.getFavoritePromises().first()
                Log.d("CategoryViewModel", "Loaded ${_favoritePromises.value.size} favorite promises")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error loading favorite promises: ${e.message}", e)
                _error.value = "Failed to load favorite promises: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Creates a new promise.
     * Note: The `title` parameter should be the actual promise title (e.g., "Trust in God").
     * The `reference` parameter should be the scripture reference (e.g., "Proverbs 3:5-6").
     * Category, Title, and Subtitle are taken from current selections.
     */
    fun createPromise(actualPromiseTitle: String, verseText: String, scriptureReference: String) {
        viewModelScope.launch {
            val category = _selectedCategory.value
            val title = _selectedTitle.value
            val subtitle = _selectedSubtitle.value

            if (category == null || title == null || subtitle == null) {
                _error.value = "A category, title, and subtitle must be selected to create a promise."
                Log.e("CategoryViewModel", "Cannot create promise: category, title, or subtitle not selected.")
                return@launch
            }

            _isLoading.value = true
            try {
                // Construct the hierarchical fields for PromiseEntity storage
                // Promise.title will store "CategoryName:ActualPromiseTitle"
                // Promise.reference will store "TitleName|SubtitleName"
                // Promise.verse will store the verse text.
                // The scriptureReference (e.g., "John 3:16") needs a place.
                // For this example, let's assume it's combined with verse or is a separate field if Promise model is extended.
                // Given current Promise model, if `reference` is for "TitleName|SubtitleName", then scriptureReference might be lost or part of `verse`.
                // Let's assume the original design intended `Promise.reference` to be for hierarchy, and `Promise.verse` holds both scripture ref and text.
                // For consistency with repository structure:
                val promiseEntityTitle = "${category.name}${PromisesRepository.CATEGORY_SEPARATOR}$actualPromiseTitle"
                val promiseEntityReference = "${title.name}${PromisesRepository.TITLE_SEPARATOR}${subtitle.name}"

                val newPromise = Promise(
                    id = 0L, // ID will be generated by DAO or is System.currentTimeMillis() in repo if id is 0.
                    title = promiseEntityTitle, // This is "Category:ActualTitle"
                    verse = "$scriptureReference - $verseText", // Combine scripture ref and text into verse for now
                    reference = promiseEntityReference // This is "Title|Subtitle"
                )

                val newId = promisesRepository.addPromise(newPromise)
                val createdPromise = newPromise.copy(id = newId) // Get the promise with the actual ID

                // Update local lists
                _promises.value = _promises.value + createdPromise // Add to current list
                _recentPromises.value = (listOf(createdPromise) + _recentPromises.value).take(5)

                Log.d("CategoryViewModel", "Created new promise: '$actualPromiseTitle' with ID $newId under ${category.name}/${title.name}/${subtitle.name}")

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
            // This is a mock implementation as 'favorite' isn't a DB field
            // In a real app, you'd update a 'isFavorite' flag in the DB via repository
            try {
                val promise = (_promises.value.find { it.id == promiseId }
                    ?: _recentPromises.value.find { it.id == promiseId }
                    ?: _favoritePromises.value.find {it.id == promiseId })

                promise?.let {
                    val isCurrentlyFavorite = _favoritePromises.value.any { fav -> fav.id == promiseId }
                    if (isCurrentlyFavorite) {
                        _favoritePromises.value = _favoritePromises.value.filterNot { fav -> fav.id == promiseId }
                        Log.d("CategoryViewModel", "Promise ${it.id} removed from favorites.")
                    } else {
                        _favoritePromises.value = (listOf(it) + _favoritePromises.value).distinctBy { it.id }
                        Log.d("CategoryViewModel", "Promise ${it.id} added to favorites.")
                    }
                    // If you had a real 'isFavorite' field:
                    // val updatedPromise = it.copy(isFavorite = !it.isFavorite)
                    // promisesRepository.updatePromise(updatedPromise)
                    // Then reloadFavoritePromises() or update lists manually
                }
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error toggling favorite: ${e.message}", e)
                _error.value = "Error updating favorite status."
            }
        }
    }

    // Clear error message
    fun clearError() {
        _error.value = null
    }

    fun debugTitleCreation(categoryName: String, titleName: String) {
        viewModelScope.launch {
            try {
                // Direct low-level insertion
                val entity = PromiseEntity(
                    id = System.currentTimeMillis(),
                    title = "$categoryName:", // Explicit format with colon
                    verse = "Debug title entry",
                    reference = "$titleName|General|Debug"
                )

                // Insert directly via DAO
                promisesRepository.promiseDao.insertPromise(entity)

                // List all entities matching this category
                val entities = promisesRepository.promiseDao.getPromisesByCategory(categoryName).first()

                Log.d("DEBUG", "===== DATABASE CONTENT =====")
                Log.d("DEBUG", "Created test title: $categoryName:$titleName")
                Log.d("DEBUG", "Found ${entities.size} entities for category '$categoryName':")

                entities.forEach { entity ->
                    Log.d("DEBUG", "Entity: id=${entity.id}, title='${entity.title}', ref='${entity.reference}'")

                    // Extract and log title part
                    val titlePart = entity.reference.split(PromisesRepository.TITLE_SEPARATOR).firstOrNull()?.trim()
                    Log.d("DEBUG", "  -> Extracted title: '$titlePart'")
                }

                // Force refresh
                loadTitlesByCategory(categoryName)
                Log.d("DEBUG", "===========================")
            } catch (e: Exception) {
                Log.e("DEBUG", "Debug error: ${e.message}", e)
            }
        }
    }

}