// app/src/main/java/com/example/quotex/data/db/QuoteXDatabase.kt
package com.example.quotex.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.quotex.data.db.dao.PromiseDao
import com.example.quotex.data.db.entities.PromiseEntity

@Database(entities = [PromiseEntity::class], version = 1, exportSchema = false)
abstract class QuoteXDatabase : RoomDatabase() {
    abstract fun promiseDao(): PromiseDao
}