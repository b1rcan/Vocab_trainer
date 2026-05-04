package com.example.vocabtrainer.data
import com.example.vocabtrainer.data.local.AppDatabase
import com.example.vocabtrainer.data.local.Word
import com.example.vocabtrainer.data.remote.RetrofitClient
import com.example.vocabtrainer.data.remote.model.DictionaryResponse
sealed class Result<out T> { data class Success<T>(val data: T) : Result<T>(); data class Error(val msg: String) : Result<Nothing>() }
class WordRepository(db: AppDatabase) {
    private val dao = db.wordDao()
    val all = dao.all(); val learned = dao.learned(); val unlearned = dao.unlearned()
    val weak = dao.weak(); val daily = dao.daily()
    suspend fun insert(w: Word) = dao.insert(w)
    suspend fun update(w: Word) = dao.update(w)
    suspend fun delete(w: Word) = dao.delete(w)
    suspend fun random(n: Int) = dao.random(n)
    fun search(q: String) = dao.search(q)
    suspend fun markLearned(w: Word) = dao.update(w.copy(isLearned = true))
    suspend fun markCorrect(w: Word) = dao.update(w.copy(correctCount = w.correctCount + 1, lastReviewDate = System.currentTimeMillis()))
    suspend fun markWrong(w: Word) = dao.update(w.copy(wrongCount = w.wrongCount + 1, lastReviewDate = System.currentTimeMillis()))
    suspend fun fetchWord(word: String): Result<DictionaryResponse> = try {
        val r = RetrofitClient.api.define(word)
        if (r.isSuccessful && r.body() != null) Result.Success(r.body()!!.first()) else Result.Error("Bulunamadı")
    } catch (e: Exception) { Result.Error(e.message ?: "Hata") }
}
