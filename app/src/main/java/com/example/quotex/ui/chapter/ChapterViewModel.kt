package com.example.quotex.ui.chapter

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotex.data.repository.ProverbsRepository
import com.example.quotex.data.source.ProverbsDataSource
import com.example.quotex.model.ProverbVerse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChapterViewModel @Inject constructor(
    private val proverbsRepository: ProverbsRepository,
    private val proverbsDataSource: ProverbsDataSource
) : ViewModel() {

    private val _verses = MutableLiveData<List<ProverbVerse>>(emptyList())
    val verses: LiveData<List<ProverbVerse>> = _verses

    private var allVerses: List<ProverbVerse> = emptyList()
    private var currentChapter: Int = 1

    fun loadVersesForChapter(chapterNumber: Int) {
        viewModelScope.launch {
            try {
                currentChapter = chapterNumber

                // Use proverbsDataSource directly since repository doesn't expose the method
                val proverbs = proverbsDataSource.getProverbsForChapter(chapterNumber)

                // Extract the verses
                allVerses = proverbs.mapIndexed { index, quote ->
                    val verseNumber = extractVerseNumber(quote.reference)
                    ProverbVerse(
                        verse = verseNumber,
                        text = quote.text
                    )
                }

                // Sort verses by their number
                allVerses = allVerses.sortedBy { it.verse }

                _verses.value = allVerses
                Log.d("ChapterViewModel", "Loaded ${allVerses.size} verses for chapter $chapterNumber")
            } catch (e: Exception) {
                Log.e("ChapterViewModel", "Error loading verses: ${e.message}", e)
            }
        }
    }

    fun filterVerses(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _verses.value = allVerses
            } else {
                _verses.value = allVerses.filter {
                    it.text.contains(query, ignoreCase = true)
                }
            }
        }
    }

    private fun extractVerseNumber(reference: String): Int {
        return try {
            // Format is typically "Proverbs X:Y"
            val verse = reference.split(":").lastOrNull()?.trim()?.toIntOrNull() ?: 1
            verse
        } catch (e: Exception) {
            Log.e("ChapterViewModel", "Error extracting verse number from $reference: ${e.message}")
            1
        }
    }

    // For direct loading without using the repository (if needed)
    fun setVersesDirectly(verses: List<ProverbVerse>) {
        allVerses = verses
        _verses.value = verses
    }
}