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
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing Promises, Categories, Titles, and Subtitles
 * Note: In a real app, you would have separate tables for Categories, Titles, and Subtitles
 * This implementation simulates that by storing the hierarchy in the Promise entities
 */
@Singleton
class PromisesRepository @Inject constructor(
    private val promiseDao: PromiseDao
) {
    companion object {
        private const val TAG = "PromisesRepository"
        private const val CATEGORY_SEPARATOR = ":"
        private const val TITLE_SEPARATOR = "|"
        private const val SUBTITLE_SEPARATOR = ">"
    }

    init {
        Log.d(TAG, "Repository initialized with DAO: ${promiseDao.javaClass.simpleName}")
    }

    // ---- Promise CRUD operations ----

    /**
     * Get all promises as a Flow
     */
    fun getAllPromises(): Flow<List<Promise>> =
        promiseDao.getAllPromises()
            .distinctUntilChanged()
            .map { entities ->
                entities.map { it.toPromise() }
            }
            .catch { e ->
                Log.e(TAG, "Error getting promises: ${e.message}", e)
                emit(emptyList())
            }

    /**
     * Add a new promise
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
     */
    fun searchPromises(query: String): Flow<List<Promise>> =
        promiseDao.searchPromises(query)
            .map { entities ->
                entities.map { it.toPromise() }
            }
            .catch { e ->
                Log.e(TAG, "Error searching promises: ${e.message}", e)
                emit(emptyList())
            }

    // ---- Category operations ----

    /**
     * Get all unique categories from promise titles
     */
    fun getCategories(): Flow<List<Category>> =
        promiseDao.getAllPromises()
            .map { entities ->
                entities
                    .map { it.title.split(CATEGORY_SEPARATOR).first().trim() }
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
     * Updates all promises with the old category name to use the new name
     */
    suspend fun renameCategory(oldName: String, newName: String) {
        try {
            // Get all promises with the old category name
            val promisesToUpdate = promiseDao.getPromisesByCategory(oldName)

            // Update each promise with the new category name
            promisesToUpdate.forEach { entity ->
                val updatedTitle = entity.title.replace(oldName, newName)
                promiseDao.updatePromise(entity.copy(title = updatedTitle))
            }

            Log.d(TAG, "Category renamed from '$oldName' to '$newName', updated ${promisesToUpdate.size} promises")
        } catch (e: Exception) {
            Log.e(TAG, "Error renaming category: ${e.message}", e)
            throw e
        }
    }

    /**
     * Delete all promises in a category
     */
    suspend fun deleteCategory(categoryName: String) {
        try {
            val deletedCount = promiseDao.deletePromisesByCategory(categoryName)
            Log.d(TAG, "Category deleted: $categoryName, removed $deletedCount promises")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting category: ${e.message}", e)
            throw e
        }
    }

    // ---- Title operations ----

    /**
     * Get all titles for a specific category
     */
    fun getTitlesByCategory(categoryName: String): Flow<List<Title>> =
        promiseDao.getPromisesByCategory(categoryName)
            .map { entities ->
                // Extract titles from the metadata (reference field in this simplified example)
                entities
                    .mapNotNull {
                        val titlePart = it.reference.split(TITLE_SEPARATOR).firstOrNull()
                        titlePart?.let { title ->
                            // Use format: "TITLE|Subtitle"
                            title.takeIf { it.isNotBlank() }
                        }
                    }
                    .distinct()
                    .sorted()
                    .mapIndexed { index, name ->
                        // Find the appropriate categoryId from the Category list
                        // In a real app, this would be stored in the Title entity
                        Title(
                            id = index.toLong() + 1,
                            categoryId = index.toLong() + 1, // Simulate categoryId
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
     */
    suspend fun renameTitle(categoryName: String, oldTitle: String, newTitle: String) {
        try {
            // Get all promises with the old title
            val promises = promiseDao.getPromisesByCategoryAndTitle(categoryName, oldTitle)

            // Update each promise with the new title
            promises.forEach { entity ->
                val updatedReference = entity.reference.replace("$oldTitle$TITLE_SEPARATOR", "$newTitle$TITLE_SEPARATOR")
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
     */
    suspend fun deleteTitle(categoryName: String, titleName: String) {
        try {
            val deletedCount = promiseDao.deletePromisesByCategoryAndTitle(categoryName, titleName)
            Log.d(TAG, "Title deleted: $titleName in category $categoryName, removed $deletedCount promises")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting title: ${e.message}", e)
            throw e
        }
    }

    // ---- Subtitle operations ----

    /**
     * Get all subtitles for a specific title in a category
     */
    fun getSubtitlesByTitle(categoryName: String, titleName: String): Flow<List<Subtitle>> =
        promiseDao.getPromisesByCategoryAndTitle(categoryName, titleName)
            .map { entities ->
                // Extract subtitles from the metadata
                entities
                    .mapNotNull {
                        val parts = it.reference.split(TITLE_SEPARATOR)
                        if (parts.size > 1) {
                            // Format: "TITLE|SUBTITLE"
                            parts[1].takeIf { it.isNotBlank() }
                        } else null
                    }
                    .distinct()
                    .sorted()
                    .mapIndexed { index, name ->
                        Subtitle(
                            id = index.toLong() + 1,
                            titleId = index.toLong() + 1, // Simulate titleId
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
     */
    suspend fun renameSubtitle(categoryName: String, titleName: String, oldSubtitle: String, newSubtitle: String) {
        try {
            // Get all promises with the old subtitle
            val promises = promiseDao.getPromisesByCategoryTitleAndSubtitle(categoryName, titleName, oldSubtitle)

            // Update each promise with the new subtitle
            promises.forEach { entity ->
                val updatedReference = entity.reference.replace("$titleName$TITLE_SEPARATOR$oldSubtitle", "$titleName$TITLE_SEPARATOR$newSubtitle")
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
     */
    suspend fun deleteSubtitle(categoryName: String, titleName: String, subtitleName: String) {
        try {
            val deletedCount = promiseDao.deletePromisesByCategoryTitleAndSubtitle(categoryName, titleName, subtitleName)
            Log.d(TAG, "Subtitle deleted: $subtitleName in title $titleName, removed $deletedCount promises")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting subtitle: ${e.message}", e)
            throw e
        }
    }

    // ---- Helper and utility methods ----

    /**
     * Add helper function to check database state
     */
    suspend fun getDatabaseState(): String {
        return try {
            val count = promiseDao.countPromises()
            "Database contains $count promises"
        } catch (e: Exception) {
            "Database error: ${e.message}"
        }
    }

    /**
     * Get the most recent promises, sorted by ID (newest first)
     */
    fun getRecentPromises(limit: Int = 5): Flow<List<Promise>> =
        promiseDao.getRecentPromises(limit)
            .map { entities ->
                entities.map { it.toPromise() }
            }
            .catch { e ->
                Log.e(TAG, "Error getting recent promises: ${e.message}", e)
                emit(emptyList())
            }

    /**
     * Get favorite promises (in a real app, you would have a 'favorite' flag in the database)
     * This simulation just returns the first few promises
     */
    fun getFavoritePromises(limit: Int = 5): Flow<List<Promise>> =
        promiseDao.getAllPromises()
            .map { entities ->
                entities.take(limit).map { it.toPromise() }
            }
            .catch { e ->
                Log.e(TAG, "Error getting favorite promises: ${e.message}", e)
                emit(emptyList())
            }
}