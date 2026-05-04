package com.example.vocabtrainer.ui.wordlearning

import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.vocabtrainer.data.local.Word
import com.example.vocabtrainer.databinding.ActivityWordLearningBinding
import com.example.vocabtrainer.ui.WordViewModel
import java.util.Locale

class WordLearningActivity : AppCompatActivity() {

    private lateinit var b: ActivityWordLearningBinding
    private val vm: WordViewModel by viewModels()

    private var words: List<Word> = emptyList()
    private var idx = 0

    private var tts: TextToSpeech? = null
    private var mp: MediaPlayer? = null

    private fun updateWords(newWords: List<Word>) {
        val isFirstLoad = words.isEmpty()
        words = newWords

        when {
            // İlk yükleme — kelime var → göster
            isFirstLoad && newWords.isNotEmpty() -> show(0)

            // İlk yükleme — kelime yok → tebrikler, çık
            isFirstLoad && newWords.isEmpty() -> {
                Toast.makeText(this, "Tebrikler! Tüm kelimeleri öğrendiniz 🎉", Toast.LENGTH_LONG).show()
                finish()
            }

            // Sonraki Room güncellemeleri — idx'i koru, ekranı yenile
            !isFirstLoad && newWords.isNotEmpty() -> show(idx)

            // Kelime öğrenildi, liste bitti → bitir
            !isFirstLoad && newWords.isEmpty() -> {
                Toast.makeText(this, "Tebrikler! Tüm kelimeleri öğrendiniz 🎉", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityWordLearningBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnBack.setOnClickListener { finish() }

        val mode = intent.getStringExtra("mode") ?: "all"
        when (mode) {
            "weak"  -> vm.weak.observe(this)      { updateWords(it) }
            "daily" -> vm.dailyWords.observe(this) { updateWords(it) }
            else    -> vm.unlearned.observe(this)  { updateWords(it) }
        }

        b.btnLearned.setOnClickListener { words.getOrNull(idx)?.let { vm.markLearned(it); next() } }
        b.btnAgain.setOnClickListener { next() }
        b.btnAudio.setOnClickListener { words.getOrNull(idx)?.let { speak(it.word) } }

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Dil desteklenmiyor", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "TTS başlatılamadı", Toast.LENGTH_SHORT).show()
            }
        }
        tts?.setSpeechRate(0.9f)
        tts?.setPitch(1.0f)
    }

    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun show(i: Int) {
        if (i >= words.size) {
            Toast.makeText(this, "Tebrikler! 🎉", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        idx = i
        val w = words[i]
        b.tvWord.text     = w.word
        b.tvPhonetic.text = w.phonetic.ifEmpty { "/${w.word}/" }
        b.tvMeaning.text  = w.meaning
        b.tvExample.text  = if (w.exampleSentence.isNotEmpty()) "\"${w.exampleSentence}\"" else ""
        b.tvIndex.text    = "${i + 1}/${words.size}"
        b.progressWord.progress = ((i + 1) * 100 / words.size)
    }

    private fun next() {
        if (idx + 1 < words.size) show(idx + 1)
        else { Toast.makeText(this, "Son kelime!", Toast.LENGTH_SHORT).show(); finish() }
    }

    override fun onDestroy() {
        super.onDestroy()
        mp?.release()
        tts?.stop()
        tts?.shutdown()
    }
}