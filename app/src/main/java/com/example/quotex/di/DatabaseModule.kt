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
            .fallbackToDestructiveMigration() // Recreate database if migration isn't provided
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Log.d("DatabaseModule", "Database created successfully")

                    // Add initial data directly with SQL
                    try {
                        val initialPromiseSql = "INSERT INTO promises (id, title, verse, reference) " +
                                "VALUES (1, 'Initial Promise', 'This is a promise created on database creation', 'Genesis 1:1')"
                        db.execSQL(initialPromiseSql)
                        Log.d("DatabaseModule", "Added initial promise via SQL")
                    } catch (e: Exception) {
                        Log.e("DatabaseModule", "Failed to insert initial promise: ${e.message}", e)
                    }
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    Log.d("DatabaseModule", "Database opened successfully")

                    // Check if database has records
                    try {
                        val cursor = db.query("SELECT COUNT(*) FROM promises")
                        cursor.moveToFirst()
                        val count = cursor.getInt(0)
                        cursor.close()
                        Log.d("DatabaseModule", "Database has $count promises upon opening")
                    } catch (e: Exception) {
                        Log.e("DatabaseModule", "Error checking promise count: ${e.message}", e)
                    }
                }
            })
            .build()
    }

    @Provides
    fun providePromiseDao(database: QuoteXDatabase): PromiseDao {
        return database.promiseDao()
    }
}