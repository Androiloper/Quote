package com.example.quotex.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.quotex.model.Promise

/**
 * Room entity for storing promises in the database
 *
 * The hierarchical structure is encoded in the title and reference fields:
 * - title: "CategoryName:ActualPromiseTitle"
 * - reference: "TitleName|SubtitleName - ScriptureReference"
 */
@Entity(tableName = "promises")
data class PromiseEntity(
    @PrimaryKey val id: Long = System.currentTimeMillis(),
    val title: String,  // Stores "CategoryName:PromiseTitle"
    val verse: String,  // Stores the verse text
    val reference: String // Stores "TitleName|SubtitleName - ScriptureReference"
) {
    /**
     * Convert this entity to a domain model Promise
     */
    fun toPromise(): Promise = Promise(
        id = id,
        title = title,
        verse = verse,
        reference = reference
    )

    companion object {
        /**
         * Create a PromiseEntity from a domain model Promise
         */
        fun fromPromise(promise: Promise): PromiseEntity = PromiseEntity(
            id = if (promise.id == 0L) System.currentTimeMillis() else promise.id,
            title = promise.title,
            verse = promise.verse,
            reference = promise.reference
        )
    }
}