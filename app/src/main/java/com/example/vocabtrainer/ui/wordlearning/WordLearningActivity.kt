package com.example.vocabtrainer.ui.wordlearning
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.vocabtrainer.data.Result
import com.example.vocabtrainer.data.local.Word
import com.example.vocabtrainer.databinding.ActivityWordLearningBinding
import com.example.vocabtrainer.ui.WordViewModel
class WordLearningActivity : AppCompatActivity() {
    private lateinit var b: ActivityWordLearningBinding
    private val vm: WordViewModel by viewModels()
    private var words: List<Word> = emptyList()
    private var idx = 0
    private var mp: MediaPlayer? = null
    override fun onCreate(s: Bundle?) {
        super.onCreate(s); b = ActivityWordLearningBinding.inflate(layoutInflater); setContentView(b.root)
        b.btnBack.setOnClickListener { finish() }
        val mode = intent.getStringExtra("mode") ?: "all"
        when (mode) {
            "weak" -> vm.weak.observe(this) { words = it; if (it.isNotEmpty()) show(0) }
            "daily" -> vm.daily.observe(this) { words = it; if (it.isNotEmpty()) show(0) }
            else -> vm.unlearned.observe(this) { words = it; if (it.isNotEmpty()) show(0) }
        }
        b.btnLearned.setOnClickListener { words.getOrNull(idx)?.let { vm.markLearned(it); next() } }
        b.btnAgain.setOnClickListener { next() }
        b.btnAudio.setOnClickListener {
            words.getOrNull(idx)?.let { w ->
                if (w.audioUrl.isNotEmpty()) play(w.audioUrl) else fetchAudio(w.word)
            }
        }
    }
    private fun show(i: Int) {
        if (i >= words.size) { Toast.makeText(this, "Tebrikler! 🎉", Toast.LENGTH_LONG).show(); finish(); return }
        idx = i; val w = words[i]
        b.tvWord.text = w.word
        b.tvPhonetic.text = w.phonetic.ifEmpty { "/${w.word}/" }
        b.tvMeaning.text = w.meaning
        b.tvExample.text = if (w.exampleSentence.isNotEmpty()) "\"${w.exampleSentence}\"" else ""
        b.tvIndex.text = "${i+1}/${words.size}"
        b.progressWord.progress = ((i+1)*100/words.size)
    }
    private fun next() { if (idx+1 < words.size) show(idx+1) else { Toast.makeText(this,"Son kelime!",Toast.LENGTH_SHORT).show(); finish() } }
    private fun fetchAudio(word: String) {
        vm.fetchWord(word)
        vm.apiResult.observe(this) { r ->
            if (r is Result.Success) {
                val url = r.data.phonetics?.firstOrNull { !it.audio.isNullOrEmpty() }?.audio
                if (!url.isNullOrEmpty()) play(url) else Toast.makeText(this,"Ses bulunamadı",Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun play(url: String) {
        mp?.release(); mp = MediaPlayer().apply {
            setDataSource(url); prepareAsync()
            setOnPreparedListener { start() }
            setOnErrorListener { _,_,_ -> Toast.makeText(this@WordLearningActivity,"Oynatılamadı",Toast.LENGTH_SHORT).show(); true }
        }
    }
    override fun onDestroy() { super.onDestroy(); mp?.release() }
}
