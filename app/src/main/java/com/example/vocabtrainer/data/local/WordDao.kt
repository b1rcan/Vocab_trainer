package com.example.vocabtrainer.data.local
import androidx.lifecycle.LiveData
import androidx.room.*
@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(w: Word)
    @Update suspend fun update(w: Word)
    @Delete suspend fun delete(w: Word)
    @Query("SELECT * FROM words ORDER BY addedDate DESC") fun all(): LiveData<List<Word>>
    @Query("SELECT * FROM words WHERE isLearned=1 ORDER BY addedDate DESC") fun learned(): LiveData<List<Word>>
    @Query("SELECT * FROM words WHERE isLearned=0") fun unlearned(): LiveData<List<Word>>
    @Query("SELECT * FROM words WHERE word LIKE '%'||:q||'%' OR meaning LIKE '%'||:q||'%'") fun search(q: String): LiveData<List<Word>>
    @Query("SELECT * FROM words WHERE wrongCount > correctCount") fun weak(): LiveData<List<Word>>
    @Query("SELECT * FROM words WHERE isLearned=0 LIMIT 20") fun daily(): LiveData<List<Word>>
    @Query("SELECT * FROM words ORDER BY RANDOM() LIMIT :n") suspend fun random(n: Int): List<Word>
}
