package com.example.quotex.data.db.dao

import androidx.room.*
import com.example.quotex.data.db.entities.PromiseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PromiseDao {
    @Query("SELECT * FROM promises ORDER BY id DESC")
    fun getAllPromises(): Flow<List<PromiseEntity>>

    @Query("SELECT * FROM promises WHERE id = :id")
    suspend fun getPromiseById(id: Long): PromiseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromise(promise: PromiseEntity)

    @Update
    suspend fun updatePromise(promise: PromiseEntity)

    @Delete
    suspend fun deletePromise(promise: PromiseEntity)

    @Query("SELECT * FROM promises WHERE title LIKE '%' || :query || '%' OR verse LIKE '%' || :query || '%' OR reference LIKE '%' || :query || '%'")
    fun searchPromises(query: String): Flow<List<PromiseEntity>>
}