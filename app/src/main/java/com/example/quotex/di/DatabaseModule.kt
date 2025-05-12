package com.example.quotex.di

import android.content.Context
import androidx.room.Room
import com.example.quotex.data.db.QuoteXDatabase
import com.example.quotex.data.db.dao.PromiseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): QuoteXDatabase {
        return Room.databaseBuilder(
            context,
            QuoteXDatabase::class.java,
            "quotex-database"
        ).build()
    }

    @Provides
    fun providePromiseDao(database: QuoteXDatabase): PromiseDao {
        return database.promiseDao()
    }
}