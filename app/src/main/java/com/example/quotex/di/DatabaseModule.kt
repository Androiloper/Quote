package com.example.quotex.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.quotex.data.db.QuoteXDatabase
import com.example.quotex.data.db.dao.PromiseDao
import com.example.quotex.data.db.entities.PromiseEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val TAG = "DatabaseModule"

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
                    Log.d(TAG, "Database created successfully")

                    // Initialize the database with sample data
                    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
                    scope.launch {
                        val database = provideDatabase(context)
                        val promiseDao = database.promiseDao()

                        // Add sample promises with categories, titles, and subtitles
                        addSamplePromises(promiseDao)

                        Log.d(TAG, "Added sample promises to the database")
                    }
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    Log.d(TAG, "Database opened successfully")

                    // Check if database has records
                    try {
                        val cursor = db.query("SELECT COUNT(*) FROM promises")
                        cursor.moveToFirst()
                        val count = cursor.getInt(0)
                        cursor.close()
                        Log.d(TAG, "Database has $count promises upon opening")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error checking promise count: ${e.message}", e)
                    }
                }
            })
            .build()
    }

    @Provides
    fun providePromiseDao(database: QuoteXDatabase): PromiseDao {
        return database.promiseDao()
    }

    /**
     * Add sample promises to the database
     */
    private suspend fun addSamplePromises(promiseDao: PromiseDao) {
        // Sample Categories with Titles, Subtitles, and Promises

        // Category 1: God's Promises
        val category1 = "God's Promises"

        // Title 1.1: Salvation
        val title1 = "Salvation"
        val subtitle1 = "Eternal Life"

        promiseDao.insertPromise(
            PromiseEntity(
                id = 1,
                title = category1,
                verse = "For God so loved the world that he gave his one and only Son, that whoever believes in him shall not perish but have eternal life.",
                reference = "$title1|$subtitle1|John 3:16"
            )
        )

        promiseDao.insertPromise(
            PromiseEntity(
                id = 2,
                title = category1,
                verse = "If you declare with your mouth, 'Jesus is Lord,' and believe in your heart that God raised him from the dead, you will be saved.",
                reference = "$title1|$subtitle1|Romans 10:9"
            )
        )

        // Title 1.2: Protection
        val title2 = "Protection"
        val subtitle2 = "Daily Care"

        promiseDao.insertPromise(
            PromiseEntity(
                id = 3,
                title = category1,
                verse = "The LORD is my shepherd, I lack nothing. He makes me lie down in green pastures, he leads me beside quiet waters, he refreshes my soul.",
                reference = "$title2|$subtitle2|Psalm 23:1-3"
            )
        )

        // Category 2: Biblical Wisdom
        val category2 = "Biblical Wisdom"

        // Title 2.1: Proverbs
        val title3 = "Proverbs"
        val subtitle3 = "Wisdom"

        promiseDao.insertPromise(
            PromiseEntity(
                id = 4,
                title = category2,
                verse = "Trust in the LORD with all your heart and lean not on your own understanding; in all your ways submit to him, and he will make your paths straight.",
                reference = "$title3|$subtitle3|Proverbs 3:5-6"
            )
        )

        promiseDao.insertPromise(
            PromiseEntity(
                id = 5,
                title = category2,
                verse = "The fear of the LORD is the beginning of wisdom, and knowledge of the Holy One is understanding.",
                reference = "$title3|$subtitle3|Proverbs 9:10"
            )
        )

        // Category 3: Personal Growth
        val category3 = "Personal Growth"

        // Title 3.1: Strength
        val title4 = "Strength"
        val subtitle4 = "Courage"

        promiseDao.insertPromise(
            PromiseEntity(
                id = 6,
                title = category3,
                verse = "I can do all this through him who gives me strength.",
                reference = "$title4|$subtitle4|Philippians 4:13"
            )
        )

        promiseDao.insertPromise(
            PromiseEntity(
                id = 7,
                title = category3,
                verse = "Have I not commanded you? Be strong and courageous. Do not be afraid; do not be discouraged, for the LORD your God will be with you wherever you go.",
                reference = "$title4|$subtitle4|Joshua 1:9"
            )
        )
    }
}