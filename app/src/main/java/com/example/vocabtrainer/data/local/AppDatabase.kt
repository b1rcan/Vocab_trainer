package com.example.vocabtrainer.data.local
import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.*
@Database(entities = [Word::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(ctx: Context) = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(ctx.applicationContext, AppDatabase::class.java, "vocab_db")
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.wordDao()?.let { dao ->
                                listOf(
                                    Word(word="Eloquent", meaning="Akıcı, belagatlı", exampleSentence="She gave an eloquent speech.", isLearned=true),
                                    Word(word="Serene", meaning="Sakin, huzurlu", exampleSentence="The lake was serene at dawn.", isLearned=true),
                                    Word(word="Vivid", meaning="Canlı, parlak", exampleSentence="He has vivid memories.", isLearned=true),
                                    Word(word="Ambiguous", meaning="Belirsiz, çift anlamlı", exampleSentence="The instructions were ambiguous."),
                                    Word(word="Persevere", meaning="Sebat etmek", exampleSentence="You must persevere to succeed."),
                                    Word(word="Meticulous", meaning="Titiz, dikkatli", exampleSentence="She is meticulous in her work."),
                                    Word(word="Resilient", meaning="Dirençli, esnek", exampleSentence="Children are often resilient."),
                                    Word(word="Benevolent", meaning="İyiliksever", exampleSentence="A benevolent leader cares for all."),
                                ).forEach { dao.insert(it) }
                            }
                        }
                    }
                }).build().also { INSTANCE = it }
        }
    }
}
