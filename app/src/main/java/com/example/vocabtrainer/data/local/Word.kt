package com.example.vocabtrainer.data.local
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "words")
data class Word(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val meaning: String,
    val exampleSentence: String = "",
    val phonetic: String = "",
    val audioUrl: String = "",
    val isLearned: Boolean = false,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val lastReviewDate: Long = 0L,
    val addedDate: Long = System.currentTimeMillis()
)
