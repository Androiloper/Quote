package com.example.quotex.data.repository

import com.example.quotex.data.source.ProverbsDataSource
import com.example.quotex.model.Quote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProverbsRepository @Inject constructor(
    private val proverbsDataSource: ProverbsDataSource
) {
    fun getProverbsForCurrentDay(): Flow<List<Quote>> = flow {
        val chapter = getCurrentChapter()
        val proverbs = proverbsDataSource.getProverbsForChapter(chapter)
        emit(proverbs)
    }

    suspend fun getRandomProverbForCurrentDay(): Quote? {
        val chapter = getCurrentChapter()
        return proverbsDataSource.getRandomProverbForChapter(chapter)
    }

    private fun getCurrentChapter(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.DAY_OF_MONTH).coerceAtMost(31)
    }
}