// app/src/main/java/com/example/quotex/data/db/entities/PromiseEntity.kt
package com.example.quotex.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.quotex.model.Promise

@Entity(tableName = "promises")
data class PromiseEntity(
    @PrimaryKey val id: Long = System.currentTimeMillis(),
    val title: String,
    val verse: String,
    val reference: String
) {
    fun toPromise(): Promise = Promise(
        id = id,
        title = title,
        verse = verse,
        reference = reference
    )

    companion object {
        fun fromPromise(promise: Promise): PromiseEntity = PromiseEntity(
            id = promise.id,
            title = promise.title,
            verse = promise.verse,
            reference = promise.reference
        )
    }
}