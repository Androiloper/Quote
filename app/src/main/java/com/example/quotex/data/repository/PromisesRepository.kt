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

@Singleton
class PromisesRepository @Inject constructor(
    val promiseDao: PromiseDao
) {
    companion object {
        private const val TAG = "PromisesRepository"
        const val CATEGORY_SEPARATOR = ":"
        const val TITLE_SEPARATOR = "|" // Used to separate TitleName|SubtitleName|ScriptureRef
    }

    init {
        Log.d(TAG, "Repository initialized with DAO: ${promiseDao.javaClass.simpleName}")
    }

    fun getAllPromises(): Flow<List<Promise>> =
        promiseDao.getAllPromises()
            .distinctUntilChanged()
            .map { entities -> entities.map { it.toPromise() } }
            .catch { e ->
                Log.e(TAG, "Error getting promises: ${e.message}", e)
                emit(emptyList())
            }

    suspend fun addPromise(promise: Promise): Long {
        return try {
            // If id is 0, Room will auto-generate. If non-zero, it's an update/replace.
            val entity = PromiseEntity.fromPromise(promise)
            val insertedId = promiseDao.insertPromise(entity)
            Log.d(TAG, "Promise added/updated with ID: $insertedId, title: ${promise.title}, ref: ${promise.reference}")
            return insertedId // This will be the new rowId from Room
        } catch (e: Exception) {
            Log.e(TAG, "Error adding promise: ${e.message}", e)
            throw e
        }
    }


    suspend fun updatePromise(promise: Promise) {
        try {
            promiseDao.updatePromise(PromiseEntity.fromPromise(promise))
            Log.d(TAG, "Promise updated: ${promise.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating promise: ${e.message}", e)
            throw e
        }
    }

    suspend fun deletePromise(promise: Promise) {
        try {
            promiseDao.deletePromise(PromiseEntity.fromPromise(promise))
            Log.d(TAG, "Promise deleted: ${promise.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting promise: ${e.message}", e)
            throw e
        }
    }

    fun searchPromises(query: String): Flow<List<Promise>> =
        promiseDao.searchPromises(query)
            .map { entities -> entities.map { it.toPromise() } }
            .catch { e ->
                Log.e(TAG, "Error searching promises: ${e.message}", e)
                emit(emptyList())
            }

    fun getCategories(): Flow<List<Category>> =
        promiseDao.getAllPromises()
            .map { entities ->
                entities
                    .mapNotNull { entity ->
                        // entity.title is "CategoryName:ActualPromiseTitle"
                        entity.title.split(CATEGORY_SEPARATOR, limit = 2).firstOrNull()?.trim()
                    }
                    .distinct()
                    .sorted()
                    .mapIndexed { index, name ->
                        Category(id = (index + 1).toLong(), name = name) // Assign temporary IDs for UI models
                    }
            }
            .catch { e ->
                Log.e(TAG, "Error getting categories: ${e.message}", e)
                emit(emptyList())
            }

    suspend fun renameCategory(oldName: String, newName: String) {
        try {
            val promisesToUpdate = promiseDao.getPromisesByCategorySync(oldName)
            promisesToUpdate.forEach { entity ->
                val actualPromiseTitle = entity.title.substringAfter(CATEGORY_SEPARATOR, "")
                val updatedEntityTitle = "$newName$CATEGORY_SEPARATOR$actualPromiseTitle"
                promiseDao.updatePromise(entity.copy(title = updatedEntityTitle))
            }
            Log.d(TAG, "Category renamed from '$oldName' to '$newName', updated ${promisesToUpdate.size} promises")
        } catch (e: Exception) {
            Log.e(TAG, "Error renaming category: ${e.message}", e)
            throw e
        }
    }

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

    fun getTitlesByCategory(categoryName: String): Flow<List<Title>> =
        promiseDao.getPromisesByCategory(categoryName) // Fetches entities where entity.title starts with "categoryName:"
            .map { entities ->
                entities
                    .mapNotNull { entity ->
                        // entity.reference is "TitleName|SubtitleName|ScriptureRef"
                        entity.reference.split(TITLE_SEPARATOR).firstOrNull()?.trim()
                    }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
                    .mapIndexed { index, name ->
                        val category = getCategories().first().find { it.name == categoryName }
                        Title(
                            id = (index + 1).toLong(), // Temporary ID for UI model
                            categoryId = category?.id ?: 0L, // Link to Category UI model ID
                            name = name
                        )
                    }
            }
            .catch { e ->
                Log.e(TAG, "Error getting titles for category $categoryName: ${e.message}", e)
                emit(emptyList())
            }

    suspend fun renameTitle(categoryName: String, oldTitleName: String, newTitleName: String) {
        try {
            val promisesToUpdate = promiseDao.getPromisesByCategoryAndTitleSync(categoryName, oldTitleName)
            promisesToUpdate.forEach { entity ->
                val parts = entity.reference.split(TITLE_SEPARATOR, limit = 3)
                val subtitlePart = if (parts.size > 1) parts[1] else ""
                val scripturePart = if (parts.size > 2) parts[2] else ""
                val updatedReference = listOfNotNull(newTitleName, subtitlePart.takeIf { it.isNotEmpty() }, scripturePart.takeIf { it.isNotEmpty() }).joinToString(TITLE_SEPARATOR)
                promiseDao.updatePromise(entity.copy(reference = updatedReference))
            }
            Log.d(TAG, "Title renamed from '$oldTitleName' to '$newTitleName' in category '$categoryName'")
        } catch (e: Exception) {
            Log.e(TAG, "Error renaming title: ${e.message}", e)
            throw e
        }
    }

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

    fun getSubtitlesByTitle(categoryName: String, titleName: String): Flow<List<Subtitle>> =
        promiseDao.getPromisesByCategoryAndTitle(categoryName, titleName)
            .map { entities ->
                entities
                    .mapNotNull { entity ->
                        // entity.reference is "TitleName|SubtitleName|ScriptureRef"
                        val parts = entity.reference.split(TITLE_SEPARATOR)
                        if (parts.size > 1) parts[1].trim() else null
                    }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
                    .mapIndexed { index, name ->
                        val title = getTitlesByCategory(categoryName).first().find { it.name == titleName }
                        Subtitle(
                            id = (index + 1).toLong(), // Temporary ID for UI model
                            titleId = title?.id ?: 0L,   // Link to Title UI model ID
                            name = name
                        )
                    }
            }
            .catch { e ->
                Log.e(TAG, "Error getting subtitles for title $titleName in category $categoryName: ${e.message}", e)
                emit(emptyList())
            }

    suspend fun renameSubtitle(categoryName: String, titleName: String, oldSubtitleName: String, newSubtitleName: String) {
        try {
            val promisesToUpdate = promiseDao.getPromisesByCategoryTitleAndSubtitleSync(categoryName, titleName, oldSubtitleName)
            promisesToUpdate.forEach { entity ->
                val parts = entity.reference.split(TITLE_SEPARATOR, limit = 3)
                val scripturePart = if (parts.size > 2) parts[2] else ""
                val updatedReference = listOfNotNull(titleName, newSubtitleName, scripturePart.takeIf { it.isNotEmpty() }).joinToString(TITLE_SEPARATOR)
                promiseDao.updatePromise(entity.copy(reference = updatedReference))
            }
            Log.d(TAG, "Subtitle renamed from '$oldSubtitleName' to '$newSubtitleName' in title '$titleName'")
        } catch (e: Exception) {
            Log.e(TAG, "Error renaming subtitle: ${e.message}", e)
            throw e
        }
    }

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

    fun getPromisesByHierarchy(categoryName: String, titleName: String, subtitleName: String): Flow<List<Promise>> =
        promiseDao.getPromisesByCategoryTitleAndSubtitle(categoryName, titleName, subtitleName)
            .map { entities -> entities.map { it.toPromise() } }
            .catch { e ->
                Log.e(TAG, "Error getting promises for hierarchy ($categoryName > $titleName > $subtitleName): ${e.message}", e)
                emit(emptyList())
            }


    fun getRecentPromises(limit: Int = 5): Flow<List<Promise>> =
        promiseDao.getRecentPromises(limit)
            .map { entities -> entities.map { it.toPromise() } }
            .catch { e ->
                Log.e(TAG, "Error getting recent promises: ${e.message}", e)
                emit(emptyList())
            }

    // Placeholder for favorite promises. In a real app, PromiseEntity would have an isFavorite field.
    fun getFavoritePromises(limit: Int = 5): Flow<List<Promise>> =
        promiseDao.getAllPromises() // Simplistic: returning first N promises as "favorites"
            .map { entities -> entities.take(limit).map { it.toPromise() } }
            .catch { e ->
                Log.e(TAG, "Error getting favorite promises: ${e.message}", e)
                emit(emptyList())
            }
}