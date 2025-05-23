package com.example.quotex.data.db.dao

import androidx.room.*
import com.example.quotex.data.db.entities.PromiseEntity
import com.example.quotex.data.repository.PromisesRepository
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Promise entities
 * Provides methods to support the hierarchical organization of promises
 */
@Dao
interface PromiseDao {
    // ----- Basic CRUD operations -----

    /**
     * Get all promises ordered by ID in descending order (newest first)
     */
    @Query("SELECT * FROM promises ORDER BY id DESC")
    fun getAllPromises(): Flow<List<PromiseEntity>>

    /**
     * Get a specific promise by ID
     */
    @Query("SELECT * FROM promises WHERE id = :id")
    suspend fun getPromiseById(id: Long): PromiseEntity?

    /**
     * Insert a new promise
     * @return The ID of the inserted promise
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromise(promise: PromiseEntity): Long

    /**
     * Update an existing promise
     */
    @Update
    suspend fun updatePromise(promise: PromiseEntity)

    /**
     * Delete a promise
     */
    @Delete
    suspend fun deletePromise(promise: PromiseEntity)

    /**
     * Search promises by text in title, verse, or reference
     */
    @Query("SELECT * FROM promises WHERE title LIKE '%' || :query || '%' OR verse LIKE '%' || :query || '%' OR reference LIKE '%' || :query || '%' ORDER BY id DESC")
    fun searchPromises(query: String): Flow<List<PromiseEntity>>

    // ----- Category related operations -----

    /**
     * Get promises by category
     * Uses LIKE pattern to match category name at the beginning of the title, followed by ':'
     */
    @Query("SELECT * FROM promises WHERE title LIKE :categoryName || '${PromisesRepository.CATEGORY_SEPARATOR}' || '%' ORDER BY id DESC")
    fun getPromisesByCategory(categoryName: String): Flow<List<PromiseEntity>>

    /**
     * Get a list of entities matching the category (sync version)
     */
    @Query("SELECT * FROM promises WHERE title LIKE :categoryName || '${PromisesRepository.CATEGORY_SEPARATOR}' || '%'")
    suspend fun getPromisesByCategorySync(categoryName: String): List<PromiseEntity>

    /**
     * Delete all promises in a category
     * @return The number of promises deleted
     */
    @Query("DELETE FROM promises WHERE title LIKE :categoryName || '${PromisesRepository.CATEGORY_SEPARATOR}' || '%'")
    suspend fun deletePromisesByCategory(categoryName: String): Int

    // ----- Title related operations -----

    /**
     * Get promises by category and title
     * Searches for category in title field and title prefix in reference field
     */
    @Query("SELECT * FROM promises WHERE title LIKE :categoryName || '${PromisesRepository.CATEGORY_SEPARATOR}' || '%' AND reference LIKE :titleName || '${PromisesRepository.TITLE_SEPARATOR}' || '%' ORDER BY id DESC")
    fun getPromisesByCategoryAndTitle(categoryName: String, titleName: String): Flow<List<PromiseEntity>>

    /**
     * Get a list of entities matching the category and title (sync version)
     */
    @Query("SELECT * FROM promises WHERE title LIKE :categoryName || '${PromisesRepository.CATEGORY_SEPARATOR}' || '%' AND reference LIKE :titleName || '${PromisesRepository.TITLE_SEPARATOR}' || '%'")
    suspend fun getPromisesByCategoryAndTitleSync(categoryName: String, titleName: String): List<PromiseEntity>

    /**
     * Delete all promises in a specific title in a category
     * @return The number of promises deleted
     */
    @Query("DELETE FROM promises WHERE title LIKE :categoryName || '${PromisesRepository.CATEGORY_SEPARATOR}' || '%' AND reference LIKE :titleName || '${PromisesRepository.TITLE_SEPARATOR}' || '%'")
    suspend fun deletePromisesByCategoryAndTitle(categoryName: String, titleName: String): Int

    // ----- Subtitle related operations -----

    /**
     * Get promises by category, title, and subtitle
     * Searches for the exact match of category, title, and subtitle in the formatted fields
     */
    @Query("""
        SELECT * FROM promises 
        WHERE title LIKE :categoryName || '${PromisesRepository.CATEGORY_SEPARATOR}' || '%' 
        AND reference LIKE :titleName || '${PromisesRepository.TITLE_SEPARATOR}' || :subtitleName || '%'
        ORDER BY id DESC
    """)
    fun getPromisesByCategoryTitleAndSubtitle(
        categoryName: String,
        titleName: String,
        subtitleName: String
    ): Flow<List<PromiseEntity>>

    /**
     * Get a list of entities matching the category, title, and subtitle (sync version)
     */
    @Query("""
        SELECT * FROM promises 
        WHERE title LIKE :categoryName || '${PromisesRepository.CATEGORY_SEPARATOR}' || '%' 
        AND reference LIKE :titleName || '${PromisesRepository.TITLE_SEPARATOR}' || :subtitleName || '%'
    """)
    suspend fun getPromisesByCategoryTitleAndSubtitleSync(
        categoryName: String,
        titleName: String,
        subtitleName: String
    ): List<PromiseEntity>

    /**
     * Delete all promises in a specific subtitle in a title in a category
     * @return The number of promises deleted
     */
    @Query("""
        DELETE FROM promises 
        WHERE title LIKE :categoryName || '${PromisesRepository.CATEGORY_SEPARATOR}' || '%' 
        AND reference LIKE :titleName || '${PromisesRepository.TITLE_SEPARATOR}' || :subtitleName || '%'
    """)
    suspend fun deletePromisesByCategoryTitleAndSubtitle(
        categoryName: String,
        titleName: String,
        subtitleName: String
    ): Int

    // ----- Helper and utility operations -----

    /**
     * Count total promises
     */
    @Query("SELECT COUNT(*) FROM promises")
    suspend fun countPromises(): Int

    /**
     * Get recent promises, ordered by ID descending (newest first)
     */
    @Query("SELECT * FROM promises ORDER BY id DESC LIMIT :limit")
    fun getRecentPromises(limit: Int): Flow<List<PromiseEntity>>
}