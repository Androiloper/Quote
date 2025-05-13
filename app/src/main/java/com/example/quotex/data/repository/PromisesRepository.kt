package com.example.quotex.data.repository

import android.util.Log
import com.example.quotex.data.db.dao.PromiseDao
import com.example.quotex.data.db.entities.PromiseEntity
import com.example.quotex.model.Promise
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromisesRepository @Inject constructor(
    private val promiseDao: PromiseDao
) {
    fun getAllPromises(): Flow<List<Promise>> =
        promiseDao.getAllPromises().map { entities ->
            entities.map { it.toPromise() }
        }.catch { e ->
            Log.e("PromisesRepository", "Error getting promises: ${e.message}", e)
            emit(emptyList())
        }

    suspend fun addPromise(promise: Promise) {
        try {
            promiseDao.insertPromise(PromiseEntity.fromPromise(promise))
            Log.d("PromisesRepository", "Promise added: ${promise.title}")
        } catch (e: Exception) {
            Log.e("PromisesRepository", "Error adding promise: ${e.message}", e)
            throw e
        }
    }

    suspend fun updatePromise(promise: Promise) {
        try {
            promiseDao.updatePromise(PromiseEntity.fromPromise(promise))
            Log.d("PromisesRepository", "Promise updated: ${promise.title}")
        } catch (e: Exception) {
            Log.e("PromisesRepository", "Error updating promise: ${e.message}", e)
            throw e
        }
    }

    suspend fun deletePromise(promise: Promise) {
        try {
            promiseDao.deletePromise(PromiseEntity.fromPromise(promise))
            Log.d("PromisesRepository", "Promise deleted: ${promise.title}")
        } catch (e: Exception) {
            Log.e("PromisesRepository", "Error deleting promise: ${e.message}", e)
            throw e
        }
    }

    fun searchPromises(query: String): Flow<List<Promise>> =
        promiseDao.searchPromises(query).map { entities ->
            entities.map { it.toPromise() }
        }.catch { e ->
            Log.e("PromisesRepository", "Error searching promises: ${e.message}", e)
            emit(emptyList())
        }
}