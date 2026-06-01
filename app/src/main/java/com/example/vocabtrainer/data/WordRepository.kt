package com.example.vocabtrainer.data

import com.example.vocabtrainer.data.local.AppDatabase
import com.example.vocabtrainer.data.local.Word
import com.example.vocabtrainer.data.remote.RetrofitClient
import com.example.vocabtrainer.data.remote.model.DictionaryResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val msg: String) : Result<Nothing>()
}

class WordRepository(db: AppDatabase) {
    private val dao = db.wordDao()
    val all = dao.all()
    val learned = dao.learned()
    val unlearned = dao.unlearned()
    val weak = dao.weak()
    val daily = dao.daily()

    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid

    suspend fun insert(w: Word) {
        dao.insert(w)
        uid?.let {
            val ref = FirebaseDatabase.getInstance().getReference("users/$it/words")
            ref.push().setValue(w)
        }
    }

    suspend fun update(w: Word) {
        dao.update(w)
        uid?.let {
            FirebaseDatabase.getInstance().getReference("users/$it/words/${w.id}").setValue(w)
        }
    }

    suspend fun delete(w: Word) {
        dao.delete(w)
        uid?.let {
            FirebaseDatabase.getInstance().getReference("users/$it/words/${w.id}").removeValue()
        }
    }

    suspend fun random(n: Int) = dao.random(n)
    fun search(q: String) = dao.search(q)
    suspend fun markLearned(w: Word) = update(w.copy(isLearned = true))
    suspend fun markCorrect(w: Word) = update(w.copy(correctCount = w.correctCount + 1, lastReviewDate = System.currentTimeMillis()))
    suspend fun markWrong(w: Word) = update(w.copy(wrongCount = w.wrongCount + 1, lastReviewDate = System.currentTimeMillis()))

    suspend fun fetchWord(word: String): Result<DictionaryResponse> = try {
        val r = RetrofitClient.api.define(word)
        if (r.isSuccessful && r.body() != null) Result.Success(r.body()!!.first())
        else Result.Error("Bulunamadı")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Hata")
    }


    fun syncFromFirebase() {
        val ref = FirebaseDatabase.getInstance().getReference("/")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                GlobalScope.launch(Dispatchers.IO) {
                    for (child in snapshot.children) {
                        val w = child.getValue(Word::class.java) ?: continue
                        runBlocking { dao.insert(w.copy(id = 0)) }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("Firebase", "Hata: ${error.message}")
            }
        })
    }
}