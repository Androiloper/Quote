package com.example.quotex.data.db.dao

import androidx.room.*
import com.example.quotex.data.db.entities.PromiseEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Promise entities
 * Enhanced with operations to support category, title, and subtitle management
 */
@Dao
interface PromiseDao {
    // ----- Basic CRUD operations -----

    @Query("SELECT * FROM promises ORDER BY id DESC")
    fun getAllPromises(): Flow<List<PromiseEntity>>

    @Query("SELECT * FROM promises WHERE id = :id")
    suspend fun getPromiseById(id: Long): PromiseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromise(promise: PromiseEntity): Long

    @Update
    suspend fun updatePromise(promise: PromiseEntity)

    @Delete
    suspend fun deletePromise(promise: PromiseEntity)

    @Query("SELECT * FROM promises WHERE title LIKE '%' || :query || '%' OR verse LIKE '%' || :query || '%' OR reference LIKE '%' || :query || '%'")
    fun searchPromises(query: String): Flow<List<PromiseEntity>>

    // ----- Category related operations -----

    /**
     * Get promises by category
     * Uses LIKE pattern to match category name at the beginning of the title
     */
    @Query("SELECT * FROM promises WHERE title LIKE :categoryName || '%' ORDER BY id DESC")
    fun getPromisesByCategory(categoryName: String): Flow<List<PromiseEntity>>

    /**
     * Get a list of entities matching the category
     */
    @Query("SELECT * FROM promises WHERE title LIKE :categoryName || '%'")
    suspend fun getPromisesByCategorySync(categoryName: String): List<PromiseEntity>

    /**
     * Delete all promises in a category
     * @return The number of promises deleted
     */
    @Query("DELETE FROM promises WHERE title LIKE :categoryName || '%'")
    suspend fun deletePromisesByCategory(categoryName: String): Int

    // ----- Title related operations -----

    /**
     * Get promises by category and title
     * Searches for both in reference field to simulate title storage
     */
    @Query("SELECT * FROM promises WHERE title LIKE :categoryName || '%' AND reference LIKE :titleName || '|%'")
    fun getPromisesByCategoryAndTitle(categoryName: String, titleName: String): Flow<List<PromiseEntity>>

    /**
     * Get a list of entities matching the category and title
     */
    @Query("SELECT * FROM promises WHERE title LIKE :categoryName || '%' AND reference LIKE :titleName || '|%'")
    suspend fun getPromisesByCategoryAndTitleSync(categoryName: String, titleName: String): List<PromiseEntity>

    /**
     * Delete all promises in a specific title in a category
     * @return The number of promises deleted
     */
    @Query("DELETE FROM promises WHERE title LIKE :categoryName || '%' AND reference LIKE :titleName || '|%'")
    suspend fun deletePromisesByCategoryAndTitle(categoryName: String, titleName: String): Int

    // ----- Subtitle related operations -----

    /**
     * Get promises by category, title, and subtitle
     * Searches using a concatenated pattern in the reference field
     */
    @Query("SELECT * FROM promises WHERE title LIKE :categoryName || '%' AND reference LIKE :titleName || '|' || :subtitleName || '%'")
    fun getPromisesByCategoryTitleAndSubtitle(categoryName: String, titleName: String, subtitleName: String): Flow<List<PromiseEntity>>

    /**
     * Get a list of entities matching the category, title, and subtitle
     */
    @Query("SELECT * FROM promises WHERE title LIKE :categoryName || '%' AND reference LIKE :titleName || '|' || :subtitleName || '%'")
    suspend fun getPromisesByCategoryTitleAndSubtitleSync(categoryName: String, titleName: String, subtitleName: String): List<PromiseEntity>

    /**
     * Delete all promises in a specific subtitle in a title in a category
     * @return The number of promises deleted
     */
    @Query("DELETE FROM promises WHERE title LIKE :categoryName || '%' AND reference LIKE :titleName || '|' || :subtitleName || '%'")
    suspend fun deletePromisesByCategoryTitleAndSubtitle(categoryName: String, titleName: String, subtitleName: String): Int

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