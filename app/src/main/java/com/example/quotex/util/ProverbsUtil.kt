package com.example.quotex.util

import android.util.Log

/**
 * Utility functions for working with Proverbs references
 */
object ProverbsUtil {
    private const val TAG = "ProverbsUtil"

    /**
     * Extract the chapter number from a Proverbs reference
     * Example: "Proverbs 3:5" returns 3
     */
    fun extractChapterNumber(reference: String): Int {
        return try {
            // Format is typically "Proverbs X:Y"
            val parts = reference.split(" ", ":")
            val chapterIndex = parts.indexOfFirst { it.equals("Proverbs", ignoreCase = true) } + 1

            if (chapterIndex < parts.size) {
                parts[chapterIndex].toIntOrNull() ?: 1
            } else {
                Log.w(TAG, "Could not parse chapter from reference: $reference")
                1 // Default to chapter 1
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting chapter number from $reference: ${e.message}")
            1 // Default to chapter 1 if parsing fails
        }
    }

    /**
     * Extract the verse number from a Proverbs reference
     * Example: "Proverbs 3:5" returns 5
     */
    fun extractVerseNumber(reference: String): Int {
        return try {
            // Format is typically "Proverbs X:Y"
            val verse = reference.split(":").lastOrNull()?.trim()?.toIntOrNull() ?: 1
            verse
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting verse number from $reference: ${e.message}")
            1 // Default to verse 1 if parsing fails
        }
    }

    /**
     * Creates a formatted Proverbs reference
     */
    fun formatReference(chapter: Int, verse: Int): String {
        return "Proverbs $chapter:$verse"
    }
}