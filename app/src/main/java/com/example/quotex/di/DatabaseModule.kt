package com.example.quotex.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.quotex.data.db.QuoteXDatabase
import com.example.quotex.data.db.dao.PromiseDao
import com.example.quotex.data.db.entities.PromiseEntity
import com.example.quotex.data.repository.PromisesRepository // Import for separator
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
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Log.d(TAG, "Database created successfully")
                    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
                    // Get the DAO instance from the database instance directly after creation
                    // This requires a bit of a workaround or providing the DAO through the callback context
                    // For simplicity here, we'll re-fetch. In a real app, inject the DAO or pass db instance.
                    scope.launch {
                        val database = provideDatabase(context) // Re-getting, ensure this is fine or refactor
                        val promiseDao = database.promiseDao()
                        addSamplePromises(promiseDao)
                        Log.d(TAG, "Added sample promises to the database after creation.")
                    }
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    Log.d(TAG, "Database opened successfully")
                    // Optional: Check for empty and populate if necessary, if not done in onCreate
                    // This can also be a good place if onCreate is not reliably populating
                    // For example, if migrating from a version without this callback data.
                    /*
                    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
                    scope.launch {
                        val database = provideDatabase(context)
                        val promiseDao = database.promiseDao()
                        if (promiseDao.countPromises() == 0) {
                           Log.d(TAG, "Database empty onOpen, adding sample promises.")
                           addSamplePromises(promiseDao)
                        }
                    }
                    */
                }
            })
            .build()
    }

    @Provides
    fun providePromiseDao(database: QuoteXDatabase): PromiseDao {
        return database.promiseDao()
    }

    private suspend fun addSamplePromises(promiseDao: PromiseDao) {
        Log.d(TAG, "Attempting to add sample promises.")
        // Sample Categories with Titles, Subtitles, and Promises
        val catSep = PromisesRepository.CATEGORY_SEPARATOR // ":"
        val titleSep = PromisesRepository.TITLE_SEPARATOR // "|"

        // Category 1: God's Promises
        val category1 = "God's Promises"
        // Title 1.1: Salvation
        val title1_1 = "Salvation"
        val subtitle1_1_1 = "Eternal Life"
        promiseDao.insertPromise(
            PromiseEntity(
                // id will be auto-generated
                title = "$category1${catSep}Assurance of Salvation", // CategoryName:ActualPromiseTitle
                verse = "For God so loved the world that he gave his one and only Son, that whoever believes in him shall not perish but have eternal life.",
                reference = "$title1_1$titleSep$subtitle1_1_1${titleSep}John 3:16" // TitleName|SubtitleName|ScriptureRef
            )
        )
        promiseDao.insertPromise(
            PromiseEntity(
                title = "$category1${catSep}Confession of Faith",
                verse = "If you declare with your mouth, 'Jesus is Lord,' and believe in your heart that God raised him from the dead, you will be saved.",
                reference = "$title1_1$titleSep$subtitle1_1_1${titleSep}Romans 10:9"
            )
        )

        // Title 1.2: Protection
        val title1_2 = "Protection"
        val subtitle1_2_1 = "Daily Care"
        promiseDao.insertPromise(
            PromiseEntity(
                title = "$category1${catSep}The Lord is My Shepherd",
                verse = "The LORD is my shepherd, I lack nothing. He makes me lie down in green pastures, he leads me beside quiet waters, he refreshes my soul.",
                reference = "$title1_2$titleSep$subtitle1_2_1${titleSep}Psalm 23:1-3"
            )
        )

        // Category 2: Biblical Wisdom
        val category2 = "Biblical Wisdom"
        // Title 2.1: Proverbs
        val title2_1 = "Proverbs"
        val subtitle2_1_1 = "Trust and Understanding"
        promiseDao.insertPromise(
            PromiseEntity(
                title = "$category2${catSep}Trust in the Lord",
                verse = "Trust in the LORD with all your heart and lean not on your own understanding; in all your ways submit to him, and he will make your paths straight.",
                reference = "$title2_1$titleSep$subtitle2_1_1${titleSep}Proverbs 3:5-6"
            )
        )
        val subtitle2_1_2 = "Fear of the Lord"
        promiseDao.insertPromise(
            PromiseEntity(
                title = "$category2${catSep}Beginning of Wisdom",
                verse = "The fear of the LORD is the beginning of wisdom, and knowledge of the Holy One is understanding.",
                reference = "$title2_1$titleSep$subtitle2_1_2${titleSep}Proverbs 9:10"
            )
        )

        // Category 3: Personal Growth
        val category3 = "Personal Growth"
        // Title 3.1: Strength
        val title3_1 = "Strength"
        val subtitle3_1_1 = "Through Christ"
        promiseDao.insertPromise(
            PromiseEntity(
                title = "$category3${catSep}I Can Do All Things",
                verse = "I can do all this through him who gives me strength.",
                reference = "$title3_1$titleSep$subtitle3_1_1${titleSep}Philippians 4:13"
            )
        )
        val subtitle3_1_2 = "Courage and God's Presence"
        promiseDao.insertPromise(
            PromiseEntity(
                title = "$category3${catSep}Be Strong and Courageous",
                verse = "Have I not commanded you? Be strong and courageous. Do not be afraid; do not be discouraged, for the LORD your God will be with you wherever you go.",
                reference = "$title3_1$titleSep$subtitle3_1_2${titleSep}Joshua 1:9"
            )
        )
        Log.d(TAG, "Finished adding sample promises.")
    }
}