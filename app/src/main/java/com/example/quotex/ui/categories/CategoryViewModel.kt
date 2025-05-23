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

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val promisesRepository: PromisesRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory

    private val _titles = MutableStateFlow<List<Title>>(emptyList())
    val titles: StateFlow<List<Title>> = _titles

    private val _selectedTitle = MutableStateFlow<Title?>(null)
    val selectedTitle: StateFlow<Title?> = _selectedTitle

    private val _subtitles = MutableStateFlow<List<Subtitle>>(emptyList())
    val subtitles: StateFlow<List<Subtitle>> = _subtitles

    private val _selectedSubtitle = MutableStateFlow<Subtitle?>(null)
    val selectedSubtitle: StateFlow<Subtitle?> = _selectedSubtitle

    private val _promises = MutableStateFlow<List<Promise>>(emptyList())
    val promises: StateFlow<List<Promise>> = _promises

    private val _recentPromises = MutableStateFlow<List<Promise>>(emptyList())
    val recentPromises: StateFlow<List<Promise>> = _recentPromises

    private val _favoritePromises = MutableStateFlow<List<Promise>>(emptyList())
    val favoritePromises: StateFlow<List<Promise>> = _favoritePromises

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _categories.value = promisesRepository.getCategories().first()
                Log.d("CategoryViewModel", "Loaded ${_categories.value.size} categories via repository")
                _recentPromises.value = promisesRepository.getRecentPromises().first()
                _favoritePromises.value = promisesRepository.getFavoritePromises().first()
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error loading initial data: ${e.message}", e)
                _error.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _categories.value = promisesRepository.getCategories().first()
            } catch (e: Exception) {
                _error.value = "Failed to load categories: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectCategory(categoryId: Long) {
        val category = _categories.value.find { it.id == categoryId }
        _selectedCategory.value = category
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
                    _isLoading.value = false
                    return@launch
                }
                // Create a placeholder promise to establish the category
                val placeholderPromise = Promise(
                    title = "$name${PromisesRepository.CATEGORY_SEPARATOR}Initial Title Placeholder",
                    verse = "Default verse for $name.",
                    reference = "General${PromisesRepository.TITLE_SEPARATOR}General${PromisesRepository.TITLE_SEPARATOR}N/A"
                )
                promisesRepository.addPromise(placeholderPromise)
                delay(200) // Give DB some time
                loadCategories() // Refresh category list
                // Auto-select new category and load its (potentially empty or placeholder) titles
                _categories.value.find { it.name == name }?.let { newCat ->
                    selectCategory(newCat.id)
                }

                Log.d("CategoryViewModel", "Created new category: $name")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error creating category: ${e.message}", e)
                _error.value = "Failed to create category: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCategory(category: Category, newName: String) {
        if (newName.isBlank() || category.name.equals(newName, ignoreCase = true)) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (_categories.value.any { it.id != category.id && it.name.equals(newName, ignoreCase = true) }) {
                    _error.value = "Another category with name '$newName' already exists."
                    _isLoading.value = false
                    return@launch
                }
                promisesRepository.renameCategory(category.name, newName)
                delay(200)
                loadCategories()
                _selectedCategory.value?.takeIf { it.id == category.id }?.let {
                    _selectedCategory.value = it.copy(name = newName)
                    loadTitlesByCategory(newName)
                }
            } catch (e: Exception) {
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
                delay(200)
                loadCategories()
                if (_selectedCategory.value?.id == category.id) {
                    _selectedCategory.value = null
                    _titles.value = emptyList(); _selectedTitle.value = null
                    _subtitles.value = emptyList(); _selectedSubtitle.value = null
                    _promises.value = emptyList()
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete category: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTitlesByCategory(categoryName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _titles.value = promisesRepository.getTitlesByCategory(categoryName).first()
                _selectedCategory.value = _categories.value.find { it.name == categoryName }
                _selectedTitle.value = null; _subtitles.value = emptyList(); _selectedSubtitle.value = null; _promises.value = emptyList()
                Log.d("CategoryViewModel", "Loaded ${_titles.value.size} titles for category $categoryName")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error loading titles for $categoryName: ${e.message}", e)
                _error.value = "Failed to load titles: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectTitle(titleId: Long) { // titleId here is the temporary UI model ID
        val titleModel = _titles.value.find { it.id == titleId }
        _selectedTitle.value = titleModel
        _subtitles.value = emptyList(); _selectedSubtitle.value = null; _promises.value = emptyList()
        titleModel?.let { tm ->
            _selectedCategory.value?.name?.let { categoryName ->
                loadSubtitlesByTitle(categoryName, tm.name)
            } ?: run { _error.value = "Category not selected for selecting title." }
        }
    }

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
                    _isLoading.value = false
                    return@launch
                }
                if (promisesRepository.getTitlesByCategory(categoryName).first().any { it.name.equals(name, ignoreCase = true) }) {
                    _error.value = "Title '$name' already exists in category '$categoryName'."
                    _isLoading.value = false
                    return@launch
                }

                val placeholderPromise = Promise(
                    title = "$categoryName${PromisesRepository.CATEGORY_SEPARATOR}Title Placeholder",
                    verse = "Default verse for title $name in $categoryName.",
                    // Reference: TitleName|SubtitleName|ScriptureRef
                    reference = "$name${PromisesRepository.TITLE_SEPARATOR}General${PromisesRepository.TITLE_SEPARATOR}N/A"
                )
                promisesRepository.addPromise(placeholderPromise)
                delay(200)
                loadTitlesByCategory(categoryName)
                Log.d("CategoryViewModel", "Created title: $name in category $categoryName")

            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error creating title: ${e.message}", e)
                _error.value = "Failed to create title: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTitle(title: Title, newName: String) {
        if (newName.isBlank() || title.name.equals(newName, ignoreCase = true)) return
        val category = _selectedCategory.value ?: return Unit.also { _error.value = "Category not selected." }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (promisesRepository.getTitlesByCategory(category.name).first().any { it.id != title.id && it.name.equals(newName, ignoreCase = true) }) {
                    _error.value = "Another title '$newName' already exists in this category."
                    _isLoading.value = false
                    return@launch
                }
                promisesRepository.renameTitle(category.name, title.name, newName)
                delay(200)
                loadTitlesByCategory(category.name)
                _selectedTitle.value?.takeIf { it.id == title.id }?.let {
                    _selectedTitle.value = it.copy(name = newName)
                    loadSubtitlesByTitle(category.name, newName)
                }
            } catch (e: Exception) {
                _error.value = "Failed to update title: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTitle(title: Title) {
        val category = _selectedCategory.value ?: return Unit.also { _error.value = "Category not selected." }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                promisesRepository.deleteTitle(category.name, title.name)
                delay(200)
                loadTitlesByCategory(category.name)
                if (_selectedTitle.value?.id == title.id) {
                    _selectedTitle.value = null; _subtitles.value = emptyList(); _selectedSubtitle.value = null; _promises.value = emptyList()
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete title: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun loadSubtitlesByTitle(categoryName: String, titleName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _subtitles.value = promisesRepository.getSubtitlesByTitle(categoryName, titleName).first()
                _selectedTitle.value = _titles.value.find { it.name == titleName && it.categoryId == _selectedCategory.value?.id }
                _selectedSubtitle.value = null; _promises.value = emptyList()

                if (_subtitles.value.isNotEmpty()) {
                    // selectSubtitle(_subtitles.value.first().id) // Auto-select first subtitle if desired
                }
                Log.d("CategoryViewModel", "Loaded ${_subtitles.value.size} subtitles for title '$titleName' in category '$categoryName'")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error loading subtitles for $titleName: ${e.message}", e)
                _error.value = "Failed to load subtitles: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectSubtitle(subtitleId: Long) { // subtitleId is the temporary UI model ID
        val subtitleModel = _subtitles.value.find { it.id == subtitleId }
        _selectedSubtitle.value = subtitleModel
        _promises.value = emptyList()
        subtitleModel?.let { loadPromisesForSubtitle() }
    }

    fun createSubtitle(name: String, titleName: String, categoryName: String) {
        if (name.isBlank()) {
            _error.value = "Subtitle name cannot be empty."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val title = _titles.value.find { it.name == titleName && it.categoryId == _selectedCategory.value?.id }
                if (title == null) {
                    _error.value = "Title '$titleName' not found for creating subtitle."
                    _isLoading.value = false
                    return@launch
                }
                if (promisesRepository.getSubtitlesByTitle(categoryName, titleName).first().any { it.name.equals(name, ignoreCase = true) }) {
                    _error.value = "Subtitle '$name' already exists in title '$titleName'."
                    _isLoading.value = false
                    return@launch
                }

                val placeholderPromise = Promise(
                    title = "$categoryName${PromisesRepository.CATEGORY_SEPARATOR}Subtitle Placeholder",
                    verse = "Default verse for subtitle $name under $titleName.",
                    // Reference: TitleName|SubtitleName|ScriptureRef
                    reference = "$titleName${PromisesRepository.TITLE_SEPARATOR}$name${PromisesRepository.TITLE_SEPARATOR}N/A"
                )
                promisesRepository.addPromise(placeholderPromise)
                delay(200)
                loadSubtitlesByTitle(categoryName, titleName)
                Log.d("CategoryViewModel", "Created subtitle: $name under $titleName in $categoryName")
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error creating subtitle: ${e.message}", e)
                _error.value = "Failed to create subtitle: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSubtitle(subtitle: Subtitle, newName: String) {
        if (newName.isBlank() || subtitle.name.equals(newName, ignoreCase = true)) return
        val category = _selectedCategory.value ?: return Unit.also { _error.value = "Category not selected." }
        val title = _selectedTitle.value ?: return Unit.also { _error.value = "Title not selected." }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (promisesRepository.getSubtitlesByTitle(category.name, title.name).first().any { it.id != subtitle.id && it.name.equals(newName, ignoreCase = true) }) {
                    _error.value = "Another subtitle '$newName' already exists for this title."
                    _isLoading.value = false
                    return@launch
                }
                promisesRepository.renameSubtitle(category.name, title.name, subtitle.name, newName)
                delay(200)
                loadSubtitlesByTitle(category.name, title.name)
                _selectedSubtitle.value?.takeIf { it.id == subtitle.id }?.let {
                    _selectedSubtitle.value = it.copy(name = newName)
                    loadPromisesForSubtitle()
                }
            } catch (e: Exception) {
                _error.value = "Failed to update subtitle: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSubtitle(subtitle: Subtitle) {
        val category = _selectedCategory.value ?: return Unit.also { _error.value = "Category not selected." }
        val title = _selectedTitle.value ?: return Unit.also { _error.value = "Title not selected." }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                promisesRepository.deleteSubtitle(category.name, title.name, subtitle.name)
                delay(200)
                loadSubtitlesByTitle(category.name, title.name)
                if (_selectedSubtitle.value?.id == subtitle.id) {
                    _selectedSubtitle.value = null
                    _promises.value = emptyList()
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete subtitle: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

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
                    ).first()
                    Log.d("CategoryViewModel", "Loaded ${_promises.value.size} promises for ${currentCategory.name} > ${currentTitle.name} > ${currentSubtitle.name}")
                } catch (e: Exception) {
                    Log.e("CategoryViewModel", "Error loading promises for subtitle ${currentSubtitle.name}: ${e.message}", e)
                    _error.value = "Failed to load promises: ${e.message}"
                    _promises.value = emptyList()
                } finally {
                    _isLoading.value = false
                }
            } else {
                _promises.value = emptyList()
            }
        }
    }

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
                val promiseEntityFormattedTitle = "${category.name}${PromisesRepository.CATEGORY_SEPARATOR}$actualPromiseTitle"
                val promiseEntityFormattedReference = "${title.name}${PromisesRepository.TITLE_SEPARATOR}${subtitle.name}${PromisesRepository.TITLE_SEPARATOR}$scriptureReference"

                val newPromise = Promise(
                    id = 0L, // Will be auto-generated by Room
                    title = promiseEntityFormattedTitle,
                    verse = verseText,
                    reference = promiseEntityFormattedReference
                )

                val newId = promisesRepository.addPromise(newPromise)
                val createdPromise = newPromise.copy(id = newId)

                _promises.value = _promises.value + createdPromise
                // Optionally update recent promises, etc.
                // _recentPromises.value = (listOf(createdPromise) + _recentPromises.value).take(5)

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
        // Similar to existing logic, manage _favoritePromises StateFlow
    }

    fun clearError() {
        _error.value = null
    }

    fun debugTitleCreation(categoryName: String, titleName: String) {
        // This method was for debugging and might not be needed if createTitle is fixed.
    }
}