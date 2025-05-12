package com.example.quotex.data.repository

import com.example.quotex.data.db.dao.PromiseDao
import com.example.quotex.data.db.entities.PromiseEntity
import com.example.quotex.model.Promise
import kotlinx.coroutines.flow.Flow
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
        }

    suspend fun addPromise(promise: Promise) {
        promiseDao.insertPromise(PromiseEntity.fromPromise(promise))
    }

    suspend fun updatePromise(promise: Promise) {
        promiseDao.updatePromise(PromiseEntity.fromPromise(promise))
    }

    suspend fun deletePromise(promise: Promise) {
        promiseDao.deletePromise(PromiseEntity.fromPromise(promise))
    }

    fun searchPromises(query: String): Flow<List<Promise>> =
        promiseDao.searchPromises(query).map { entities ->
            entities.map { it.toPromise() }
        }
}