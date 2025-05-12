// app/src/main/java/com/example/quotex/data/source/ProverbsDataSource.kt
package com.example.quotex.data.source

import android.content.Context
import com.example.quotex.model.ProverbChapter
import com.example.quotex.model.ProverbVerse
import com.example.quotex.model.Quote
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProverbsDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var chaptersCache: Map<Int, ProverbChapter>? = null

    suspend fun getProverbsForChapter(chapter: Int): List<Quote> = withContext(Dispatchers.IO) {
        val chapters = loadChapters()
        val proverbChapter = chapters[chapter] ?: return@withContext emptyList()

        proverbChapter.verses.map { verse ->
            Quote(
                text = verse.text,
                reference = "Proverbs ${proverbChapter.chapter}:${verse.verse}"
            )
        }
    }

    suspend fun getRandomProverbForChapter(chapter: Int): Quote? = withContext(Dispatchers.IO) {
        val chapters = loadChapters()
        val proverbChapter = chapters[chapter] ?: return@withContext null

        val randomVerse = proverbChapter.verses.random()
        Quote(
            text = randomVerse.text,
            reference = "Proverbs ${proverbChapter.chapter}:${randomVerse.verse}"
        )
    }

    private suspend fun loadChapters(): Map<Int, ProverbChapter> = withContext(Dispatchers.IO) {
        if (chaptersCache != null) {
            return@withContext chaptersCache!!
        }

        try {
            val json = context.assets.open("proverbs.json").bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(json)
            val chaptersArray = jsonObject.getJSONArray("chapters")

            val chapters = mutableMapOf<Int, ProverbChapter>()

            for (i in 0 until chaptersArray.length()) {
                val chapterObj = chaptersArray.getJSONObject(i)
                val chapterNumber = chapterObj.getInt("chapter")
                val versesArray = chapterObj.getJSONArray("verses")

                val verses = mutableListOf<ProverbVerse>()
                for (j in 0 until versesArray.length()) {
                    val verseObj = versesArray.getJSONObject(j)
                    verses.add(
                        ProverbVerse(
                            verse = verseObj.getInt("verse"),
                            text = verseObj.getString("text")
                        )
                    )
                }

                chapters[chapterNumber] = ProverbChapter(
                    chapter = chapterNumber,
                    verses = verses
                )
            }

            chaptersCache = chapters
            return@withContext chapters
        } catch (e: IOException) {
            e.printStackTrace()
            return@withContext emptyMap()
        }
    }
}