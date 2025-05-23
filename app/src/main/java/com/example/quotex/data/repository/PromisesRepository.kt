package com.example.quotex.data.repository

import android.util.Log
import com.example.quotex.data.db.dao.PromiseDao
import com.example.quotex.data.db.entities.PromiseEntity
import com.example.quotex.model.Category
import com.example.quotex.model.Promise
import com.example.quotex.model.Subtitle
import com.example.quotex.model.Title
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing Promises with a hierarchical structure of Categories, Titles, and Subtitles
 *
 * This repository implements a three-level hierarchy:
 * 1. Categories (top level)
 * 2. Titles (mid-level within a category)
 * 3. Subtitles (lowest level within a title)
 *
 * The hierarchy is implemented using string formatting in the Promise entity:
 * - Promise.title: "CategoryName:ActualPromiseTitle"
 * - Promise.reference: "TitleName|SubtitleName"
 */
@Singleton
class PromisesRepository @Inject constructor(
    val promiseDao: PromiseDao
) {
    companion object {
        private const val TAG = "PromisesRepository"

        // Separators used in string formatting to implement hierarchy
        const val CATEGORY_SEPARATOR = ":"
        const val TITLE_SEPARATOR = "|"
    }

    init {
        Log.d(TAG, "Repository initialized with DAO: ${promiseDao.javaClass.simpleName}")
    }

    // ---- Promise CRUD operations ----

    /**
     * Get all promises as a Flow
     * @return Flow of all promises sorted by ID
     */
    fun getAllPromises(): Flow<List<Promise>> =
        promiseDao.getAllPromises()
            .distinctUntilChanged()
            .map { entities -> entities.map { it.toPromise() } }
            .catch { e ->
                Log.e(TAG, "Error getting promises: ${e.message}", e)
                emit(emptyList())
            }

    /**
     * Add a new promise
     * @param promise The promise to add
     * @return The ID of the newly added promise
     */
    suspend fun addPromise(promise: Promise): Long {
        return try {
            val entity = PromiseEntity.fromPromise(promise)
            val insertedId = promiseDao.insertPromise(entity)
            Log.d(TAG, "Promise added with ID: $insertedId, title: ${promise.title}")
            insertedId
        } catch (e: Exception) {
            Log.e(TAG, "Error adding promise: ${e.message}", e)
            throw e
        }
    }

    /**
     * Update an existing promise
     * @param promise The promise to update
     */
    suspend fun updatePromise(promise: Promise) {
        try {
            promiseDao.updatePromise(PromiseEntity.fromPromise(promise))
            Log.d(TAG, "Promise updated: ${promise.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating promise: ${e.message}", e)
            throw e
        }
    }

    /**
     * Delete a promise
     * @param promise The promise to delete
     */
    suspend fun deletePromise(promise: Promise) {
        try {
            promiseDao.deletePromise(PromiseEntity.fromPromise(promise))
            Log.d(TAG, "Promise deleted: ${promise.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting promise: ${e.message}", e)
            throw e
        }
    }

    /**
     * Search promises by text in title, verse, or reference
     * @param query The search query
     * @return Flow of promises matching the query
     */
    fun searchPromises(query: String): Flow<List<Promise>> =
        promiseDao.searchPromises(query)
            .map { entities -> entities.map { it.toPromise() } }
            .catch { e ->
                Log.e(TAG, "Error searching promises: ${e.message}", e)
                emit(emptyList())
            }

    // ---- Category operations ----

    /**
     * Get all unique categories from promise titles
     * @return Flow of all categories
     */
    fun getCategories(): Flow<List<Category>> =
        promiseDao.getAllPromises()
            .map { entities ->
                entities
                    .mapNotNull {
                        val parts = it.title.split(CATEGORY_SEPARATOR)
                        if (parts.isNotEmpty()) parts.first().trim() else null
                    }
                    .distinct()
                    .sorted()
                    .mapIndexed { index, name ->
                        Category(id = index.toLong() + 1, name = name)
                    }
            }
            .catch { e ->
                Log.e(TAG, "Error getting categories: ${e.message}", e)
                emit(emptyList())
            }

    /**
     * Rename a category
     * @param oldName The current name of the category
     * @param newName The new name for the category
     */
    suspend fun renameCategory(oldName: String, newName: String) {
        try {
            // Get all promises with the old category name
            val promisesToUpdate = promiseDao.getPromisesByCategorySync(oldName)

            // Update each promise with the new category name
            promisesToUpdate.forEach { entity ->
                val titleParts = entity.title.split(CATEGORY_SEPARATOR, limit = 2)
                val actualPromiseTitle = if (titleParts.size > 1) titleParts[1] else ""
                val updatedEntityTitle = "$newName$CATEGORY_SEPARATOR$actualPromiseTitle"
                promiseDao.updatePromise(entity.copy(title = updatedEntityTitle))
            }

            Log.d(TAG, "Category renamed from '$oldName' to '$newName', updated ${promisesToUpdate.size} promises")
        } catch (e: Exception) {
            Log.e(TAG, "Error renaming category: ${e.message}", e)
            throw e
        }
    }

    /**
     * Delete all promises in a category
     * @param categoryName The name of the category to delete
     * @return The number of promises deleted
     */
    suspend fun deleteCategory(categoryName: String): Int {
        return try {
            val deletedCount = promiseDao.deletePromisesByCategory(categoryName)
            Log.d(TAG, "Category deleted: $categoryName, removed $deletedCount promises")
            deletedCount
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting category: ${e.message}", e)
            throw e
        }
    }

    /**
     * Create a new category with an optional initial promise
     * @param categoryName The name of the new category
     * @param initialPromise Optional initial promise to add to the category
     * @return The ID of the category
     */
    suspend fun createCategory(categoryName: String, initialPromise: Promise? = null): Long {
        try {
            // Check if the category already exists
            val existing = getCategories().first().find { it.name == categoryName }
            if (existing != null) {
                Log.d(TAG, "Category '$categoryName' already exists with ID: ${existing.id}")
                return existing.id
            }

            // Create a placeholder promise for this category if no initial promise provided
            val promise = initialPromise ?: Promise(
                id = 0L,
                title = "$categoryName$CATEGORY_SEPARATOR",
                verse = "Placeholder for category $categoryName",
                reference = "General|Overview"
            )

            // Add the promise
            val id = addPromise(promise)
            Log.d(TAG, "Created new category: $categoryName with initial promise ID: $id")

            // Get the new category's ID
            val newCategory = getCategories().first().find { it.name == categoryName }
            return newCategory?.id ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error creating category: ${e.message}", e)
            throw e
        }
    }

    // ---- Title operations ----

    /**
     * Get all titles for a specific category
     * @param categoryName The name of the category
     * @return Flow of all titles in the category
     */
    fun getTitlesByCategory(categoryName: String): Flow<List<Title>> =
        promiseDao.getPromisesByCategory(categoryName)
            .map { entities ->
                entities
                    .mapNotNull {
                        val titlePart = it.reference.split(TITLE_SEPARATOR).firstOrNull()?.trim()
                        titlePart?.takeIf { it.isNotBlank() }
                    }
                    .distinct()
                    .sorted()
                    .mapIndexed { index, name ->
                        // Find the category ID for this category name
                        val category = getCategories().first().find { it.name == categoryName }
                        Title(
                            id = index.toLong() + 1,
                            categoryId = category?.id ?: -1L,
                            name = name
                        )
                    }
            }
            .catch { e ->
                Log.e(TAG, "Error getting titles for category $categoryName: ${e.message}", e)
                emit(emptyList())
            }

    /**
     * Rename a title within a category
     * @param categoryName The name of the category containing the title
     * @param oldTitle The current name of the title
     * @param newTitle The new name for the title
     */
    suspend fun renameTitle(categoryName: String, oldTitle: String, newTitle: String) {
        try {
            val promises = promiseDao.getPromisesByCategoryAndTitleSync(categoryName, oldTitle)
            promises.forEach { entity ->
                val parts = entity.reference.split(TITLE_SEPARATOR, limit = 2)
                val subtitlePart = if (parts.size > 1) TITLE_SEPARATOR + parts[1] else ""
                val updatedReference = "$newTitle$subtitlePart"
                promiseDao.updatePromise(entity.copy(reference = updatedReference))
            }
            Log.d(TAG, "Title renamed from '$oldTitle' to '$newTitle' in category '$categoryName'")
        } catch (e: Exception) {
            Log.e(TAG, "Error renaming title: ${e.message}", e)
            throw e
        }
    }

    /**
     * Delete all promises for a specific title in a category
     * @param categoryName The name of the category
     * @param titleName The name of the title to delete
     * @return The number of promises deleted
     */
    suspend fun deleteTitle(categoryName: String, titleName: String): Int {
        return try {
            val deletedCount = promiseDao.deletePromisesByCategoryAndTitle(categoryName, titleName)
            Log.d(TAG, "Title deleted: $titleName in category $categoryName, removed $deletedCount promises")
            deletedCount
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting title: ${e.message}", e)
            throw e
        }
    }

    /**
     * Create a new title within a category with an optional initial promise
     * @param categoryName The name of the category
     * @param titleName The name of the new title
     * @param initialPromise Optional initial promise to add to the title
     * @return The ID of the title
     */
    suspend fun createTitle(categoryName: String, titleName: String, initialPromise: Promise? = null): Long {
        try {
            // Check if the title already exists in this category
            val existing = getTitlesByCategory(categoryName).first().find { it.name == titleName }
            if (existing != null) {
                Log.d(TAG, "Title '$titleName' already exists in category '$categoryName' with ID: ${existing.id}")
                return existing.id
            }

            // Make sure the category exists
            var categoryId = getCategories().first().find { it.name == categoryName }?.id
            if (categoryId == null) {
                // Create the category if it doesn't exist
                categoryId = createCategory(categoryName)
            }

            // Create a placeholder promise for this title if no initial promise provided
            val promise = initialPromise ?: Promise(
                id = 0L,
                title = "$categoryName$CATEGORY_SEPARATOR",
                verse = "Placeholder for title $titleName",
                reference = "$titleName${TITLE_SEPARATOR}General"
            )

            // Add the promise
            val id = addPromise(promise)
            Log.d(TAG, "Created new title: $titleName in category $categoryName with initial promise ID: $id")

            // Get the new title's ID
            val newTitle = getTitlesByCategory(categoryName).first().find { it.name == titleName }
            return newTitle?.id ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error creating title: ${e.message}", e)
            throw e
        }
    }

    // ---- Subtitle operations ----

    /**
     * Get all subtitles for a specific title in a category
     * @param categoryName The name of the category
     * @param titleName The name of the title
     * @return Flow of all subtitles in the title
     */
    fun getSubtitlesByTitle(categoryName: String, titleName: String): Flow<List<Subtitle>> =
        promiseDao.getPromisesByCategoryAndTitle(categoryName, titleName)
            .map { entities ->
                entities
                    .mapNotNull {
                        val parts = it.reference.split(TITLE_SEPARATOR)
                        if (parts.size > 1) parts[1].trim() else null
                    }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
                    .mapIndexed { index, name ->
                        // Find the title ID for this title name in this category
                        val title = getTitlesByCategory(categoryName).first().find { it.name == titleName }
                        Subtitle(
                            id = index.toLong() + 1,
                            titleId = title?.id ?: -1L,
                            name = name
                        )
                    }
            }
            .catch { e ->
                Log.e(TAG, "Error getting subtitles for title $titleName in category $categoryName: ${e.message}", e)
                emit(emptyList())
            }

    /**
     * Rename a subtitle within a title in a category
     * @param categoryName The name of the category
     * @param titleName The name of the title containing the subtitle
     * @param oldSubtitle The current name of the subtitle
     * @param newSubtitle The new name for the subtitle
     */
    suspend fun renameSubtitle(categoryName: String, titleName: String, oldSubtitle: String, newSubtitle: String) {
        try {
            val promises = promiseDao.getPromisesByCategoryTitleAndSubtitleSync(categoryName, titleName, oldSubtitle)
            promises.forEach { entity ->
                val updatedReference = "$titleName$TITLE_SEPARATOR$newSubtitle"
                promiseDao.updatePromise(entity.copy(reference = updatedReference))
            }
            Log.d(TAG, "Subtitle renamed from '$oldSubtitle' to '$newSubtitle' in title '$titleName'")
        } catch (e: Exception) {
            Log.e(TAG, "Error renaming subtitle: ${e.message}", e)
            throw e
        }
    }

    /**
     * Delete all promises for a specific subtitle in a title in a category
     * @param categoryName The name of the category
     * @param titleName The name of the title
     * @param subtitleName The name of the subtitle to delete
     * @return The number of promises deleted
     */
    suspend fun deleteSubtitle(categoryName: String, titleName: String, subtitleName: String): Int {
        return try {
            val deletedCount = promiseDao.deletePromisesByCategoryTitleAndSubtitle(categoryName, titleName, subtitleName)
            Log.d(TAG, "Subtitle deleted: $subtitleName in title $titleName, removed $deletedCount promises")
            deletedCount
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting subtitle: ${e.message}", e)
            throw e
        }
    }

    /**
     * Create a new subtitle within a title with an optional initial promise
     * @param categoryName The name of the category
     * @param titleName The name of the title
     * @param subtitleName The name of the new subtitle
     * @param initialPromise Optional initial promise to add to the subtitle
     * @return The ID of the subtitle
     */
    suspend fun createSubtitle(
        categoryName: String,
        titleName: String,
        subtitleName: String,
        initialPromise: Promise? = null
    ): Long {
        try {
            // Check if the subtitle already exists in this title
            val existing = getSubtitlesByTitle(categoryName, titleName).first().find { it.name == subtitleName }
            if (existing != null) {
                Log.d(TAG, "Subtitle '$subtitleName' already exists in title '$titleName' with ID: ${existing.id}")
                return existing.id
            }

            // Make sure the title exists
            var titleId = getTitlesByCategory(categoryName).first().find { it.name == titleName }?.id
            if (titleId == null) {
                // Create the title if it doesn't exist
                titleId = createTitle(categoryName, titleName)
            }

            // Create a placeholder promise for this subtitle if no initial promise provided
            val promise = initialPromise ?: Promise(
                id = 0L,
                title = "$categoryName$CATEGORY_SEPARATOR",
                verse = "Placeholder for subtitle $subtitleName",
                reference = "$titleName$TITLE_SEPARATOR$subtitleName"
            )

            // Add the promise
            val id = addPromise(promise)
            Log.d(TAG, "Created new subtitle: $subtitleName in title $titleName with initial promise ID: $id")

            // Get the new subtitle's ID
            val newSubtitle = getSubtitlesByTitle(categoryName, titleName).first().find { it.name == subtitleName }
            return newSubtitle?.id ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error creating subtitle: ${e.message}", e)
            throw e
        }
    }

    // ---- Hierarchical promise operations ----

    /**
     * Get promises for a specific category, title, and subtitle
     * @param categoryName The name of the category
     * @param titleName The name of the title
     * @param subtitleName The name of the subtitle
     * @return Flow of promises matching the hierarchy
     */
    fun getPromisesByHierarchy(categoryName: String, titleName: String, subtitleName: String): Flow<List<Promise>> =
        promiseDao.getPromisesByCategoryTitleAndSubtitle(categoryName, titleName, subtitleName)
            .map { entities -> entities.map { it.toPromise() } }
            .catch { e ->
                Log.e(TAG, "Error getting promises for hierarchy ($categoryName > $titleName > $subtitleName): ${e.message}", e)
                emit(emptyList())
            }

    /**
     * Create a new promise in the specified hierarchical location
     * @param categoryName The category name
     * @param titleName The title name
     * @param subtitleName The subtitle name
     * @param promiseTitle The actual title of the promise
     * @param verse The verse text of the promise
     * @param scriptureReference The scripture reference of the promise
     * @return The ID of the new promise
     */
    suspend fun createPromiseInHierarchy(
        categoryName: String,
        titleName: String,
        subtitleName: String,
        promiseTitle: String,
        verse: String,
        scriptureReference: String
    ): Long {
        try {
            // Ensure the subtitle exists
            getSubtitlesByTitle(categoryName, titleName).first().find { it.name == subtitleName }
                ?: createSubtitle(categoryName, titleName, subtitleName)

            // Create the promise with the correct hierarchy encoding
            val promise = Promise(
                id = 0L,
                title = "$categoryName$CATEGORY_SEPARATOR$promiseTitle",
                verse = verse,
                reference = "$titleName$TITLE_SEPARATOR$subtitleName - $scriptureReference"
            )

            // Add the promise
            val id = addPromise(promise)
            Log.d(TAG, "Created new promise in $categoryName > $titleName > $subtitleName: $promiseTitle")
            return id
        } catch (e: Exception) {
            Log.e(TAG, "Error creating promise in hierarchy: ${e.message}", e)
            throw e
        }
    }

    // ---- Helper and utility methods ----

    /**
     * Get database status information
     * @return A string describing the current database state
     */
    suspend fun getDatabaseState(): String {
        return try {
            val count = promiseDao.countPromises()
            val categories = getCategories().first()

            val titleCounts = mutableMapOf<String, Int>()
            for (category in categories) {
                titleCounts[category.name] = getTitlesByCategory(category.name).first().size
            }

            "Database contains $count promises across ${categories.size} categories.\n" +
                    "Categories: ${categories.joinToString { "${it.name} (${titleCounts[it.name] ?: 0} titles)" }}"
        } catch (e: Exception) {
            "Database error: ${e.message}"
        }
    }

    /**
     * Get the most recent promises, sorted by ID (newest first)
     * @param limit Maximum number of promises to return
     * @return Flow of recent promises
     */
    fun getRecentPromises(limit: Int = 5): Flow<List<Promise>> =
        promiseDao.getRecentPromises(limit)
            .map { entities -> entities.map { it.toPromise() } }
            .catch { e ->
                Log.e(TAG, "Error getting recent promises: ${e.message}", e)
                emit(emptyList())
            }

    /**
     * Get favorite promises (in a real app, you would have a 'favorite' flag)
     * This simulation just returns the first few promises
     * @param limit Maximum number of promises to return
     * @return Flow of favorite promises
     */
    fun getFavoritePromises(limit: Int = 5): Flow<List<Promise>> =
        promiseDao.getAllPromises()
            .map { entities -> entities.take(limit).map { it.toPromise() } }
            .catch { e ->
                Log.e(TAG, "Error getting favorite promises: ${e.message}", e)
                emit(emptyList())
            }

    /**
     * Extract the actual promise title from the formatted title string
     * @param formattedTitle The formatted title string (e.g., "CategoryName:ActualTitle")
     * @return The actual promise title
     */
    fun extractPromiseTitle(formattedTitle: String): String {
        val parts = formattedTitle.split(CATEGORY_SEPARATOR, limit = 2)
        return if (parts.size > 1) parts[1].trim() else formattedTitle
    }

    /**
     * Extract the category name from the formatted title string
     * @param formattedTitle The formatted title string (e.g., "CategoryName:ActualTitle")
     * @return The category name
     */
    fun extractCategoryName(formattedTitle: String): String {
        val parts = formattedTitle.split(CATEGORY_SEPARATOR, limit = 2)
        return parts[0].trim()
    }

    /**
     * Extract the title name from the formatted reference string
     * @param formattedReference The formatted reference string (e.g., "TitleName|SubtitleName")
     * @return The title name
     */
    fun extractTitleName(formattedReference: String): String {
        val parts = formattedReference.split(TITLE_SEPARATOR, limit = 2)
        return parts[0].trim()
    }

    /**
     * Extract the subtitle name from the formatted reference string
     * @param formattedReference The formatted reference string (e.g., "TitleName|SubtitleName")
     * @return The subtitle name or empty string if not found
     */
    fun extractSubtitleName(formattedReference: String): String {
        val parts = formattedReference.split(TITLE_SEPARATOR, limit = 2)
        return if (parts.size > 1) parts[1].trim() else ""
    }

    /**
     * Format a promise title using the category and actual title
     * @param categoryName The name of the category
     * @param actualTitle The actual title of the promise
     * @return The formatted title string
     */
    fun formatPromiseTitle(categoryName: String, actualTitle: String): String {
        return "$categoryName$CATEGORY_SEPARATOR$actualTitle"
    }

    /**
     * Format a promise reference using the title and subtitle
     * @param titleName The name of the title
     * @param subtitleName The name of the subtitle
     * @return The formatted reference string
     */
    fun formatPromiseReference(titleName: String, subtitleName: String): String {
        return "$titleName$TITLE_SEPARATOR$subtitleName"
    }
}