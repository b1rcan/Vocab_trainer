package com.example.vocabtrainer.ui.quiz
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.vocabtrainer.data.local.Word
import com.example.vocabtrainer.databinding.ActivityQuizBinding
import com.example.vocabtrainer.ui.WordViewModel
import kotlinx.coroutines.launch
class QuizActivity : AppCompatActivity() {
    private lateinit var b: ActivityQuizBinding
    private val vm: WordViewModel by viewModels()
    private var allWords: List<Word> = emptyList()
    private var quiz: List<Word> = emptyList()
    private var idx = 0; private var score = 0
    private val opts get() = listOf(b.btnOpt1, b.btnOpt2, b.btnOpt3, b.btnOpt4)
    override fun onCreate(s: Bundle?) {
        super.onCreate(s); b = ActivityQuizBinding.inflate(layoutInflater); setContentView(b.root)
        b.btnBack.setOnClickListener { finish() }
        vm.all.observe(this) { words ->
            allWords = words
            if (words.size >= 4) { quiz = words.shuffled().take(10); show() }
            else Toast.makeText(this,"En az 4 kelime gerekli",Toast.LENGTH_SHORT).show()
        }
    }
    private fun show() {
        if (idx >= quiz.size) { Toast.makeText(this,"Bitti! $score/${quiz.size}",Toast.LENGTH_LONG).show(); finish(); return }
        val cur = quiz[idx]
        b.tvQuizWord.text = cur.word
        b.tvQuestionNum.text = "Soru ${idx+1} / ${quiz.size}"
        b.tvScore.text = "$score ✓"
        b.progressQuiz.progress = ((idx+1)*100/quiz.size)
        val labels = listOf("A","B","C","D")
        val choices = (allWords.filter{it.id!=cur.id}.shuffled().take(3) + cur).shuffled()
        opts.forEachIndexed { i, btn ->
            btn.text = "${labels[i]}) ${choices[i].meaning}"
            btn.setBackgroundResource(com.example.vocabtrainer.R.drawable.bg_white_btn)
            btn.isEnabled = true
            btn.setOnClickListener { check(btn, choices[i], cur) }
        }
    }
    private fun check(sel: Button, chosen: Word, correct: Word) {
        opts.forEach { it.isEnabled = false }
        if (chosen.id == correct.id) { sel.setBackgroundColor(Color.parseColor("#4CAF50")); vm.markCorrect(correct); score++ }
        else { sel.setBackgroundColor(Color.parseColor("#F44336")); vm.markWrong(correct)
            opts.firstOrNull { it.text.contains(correct.meaning) }?.setBackgroundColor(Color.parseColor("#4CAF50")) }
        Handler(Looper.getMainLooper()).postDelayed({ idx++; show() }, 1200)
    }
}
