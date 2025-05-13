package com.example.quotex.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
        )
            .fallbackToDestructiveMigration() // Add this for development
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Log.d("DatabaseModule", "Database created")
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    Log.d("DatabaseModule", "Database opened")
                }
            })
            .build()
    }

    @Provides
    fun providePromiseDao(database: QuoteXDatabase): PromiseDao {
        return database.promiseDao()
    }
}