package com.example.vocabtrainer.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.vocabtrainer.data.Result
import com.example.vocabtrainer.data.WordRepository
import com.example.vocabtrainer.data.local.AppDatabase
import com.example.vocabtrainer.data.local.Word
import com.example.vocabtrainer.data.remote.model.DictionaryResponse
import com.example.vocabtrainer.sync.SeenWordsPrefs
import kotlinx.coroutines.launch
import com.example.vocabtrainer.ui.wordlist.pickUnseen
const val DAILY_WORD_COUNT = 20;
class WordViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = WordRepository(AppDatabase.get(app) as AppDatabase)
    private val seenPrefs  = SeenWordsPrefs(app)

    val all      = repo.all
    val learned  = repo.learned
    val unlearned= repo.unlearned
    val weak     = repo.weak

    /**
     * Her gün/oturumda DAILY_WORD_COUNT kadar, daha önce gösterilmemiş kelime.
     * Room'daki tüm öğrenilmemiş kelimelerden SeenWordsPrefs filtresiyle seçilir.
     * Tüm kelimeler görüldüğünde seen listesi sıfırlanır ve baştan başlar.
     */
    val dailyWords: LiveData<List<Word>> = repo.unlearned.map { allUnlearned ->
        val picked = allUnlearned.pickUnseen(seenPrefs)
        seenPrefs.markSeen(picked.map { it.id })
        picked
    }

    private val _apiResult = MutableLiveData<Result<DictionaryResponse>>()
    val apiResult: LiveData<Result<DictionaryResponse>> = _apiResult

    private val _query = MutableLiveData("")
    val searchResults  = _query.switchMap { repo.search(it) }

    fun setQuery(q: String)     { _query.value = q }
    fun insert(w: Word)         = viewModelScope.launch { repo.insert(w) }
    fun markLearned(w: Word)    = viewModelScope.launch { repo.markLearned(w) }
    fun markCorrect(w: Word)    = viewModelScope.launch { repo.markCorrect(w) }
    fun markWrong(w: Word)      = viewModelScope.launch { repo.markWrong(w) }
    fun delete(w: Word)         = viewModelScope.launch { repo.delete(w) }
    fun fetchWord(word: String) = viewModelScope.launch { _apiResult.value = repo.fetchWord(word) }
    suspend fun randomWords(n: Int) = repo.random(n)
}