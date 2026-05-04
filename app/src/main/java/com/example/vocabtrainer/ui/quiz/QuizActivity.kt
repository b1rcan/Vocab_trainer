package com.example.vocabtrainer.ui.quiz

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.vocabtrainer.data.local.Word
import com.example.vocabtrainer.databinding.ActivityQuizBinding
import com.example.vocabtrainer.streak.StreakManager
import com.example.vocabtrainer.ui.WordViewModel

class QuizActivity : AppCompatActivity() {

    private lateinit var b: ActivityQuizBinding
    private val vm: WordViewModel by viewModels()
    private lateinit var streakManager: StreakManager

    private var allWords: List<Word> = emptyList()
    private var quiz:     List<Word> = emptyList()
    private var idx   = 0
    private var score = 0
    private var quizStarted = false

    private val opts get() = listOf(b.btnOpt1, b.btnOpt2, b.btnOpt3, b.btnOpt4)

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(b.root)

        streakManager = StreakManager(this)
        b.btnBack.setOnClickListener { finish() }

        // ViewModel'deki 'all' LiveData'sını gözlemliyoruz[cite: 3]
        vm.all.observe(this) { words ->
            if (words != null && !quizStarted) {
                allWords = words
                if (words.size >= 4) {
                    quiz = words.shuffled().take(10)
                    quizStarted = true
                    show()
                } else {
                    Toast.makeText(this, "Quiz için en az 4 kelime eklemelisiniz", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun show() {
        if (idx >= quiz.size) {
            val newStreak = streakManager.recordQuizCompletion()
            val msg = if (newStreak > 1) "Bitti! $score/${quiz.size} 🔥 $newStreak gün seri!"
            else "Bitti! $score/${quiz.size}"
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val cur = quiz[idx]
        b.tvQuizWord.text = cur.word
        b.tvQuestionNum.text = "Soru ${idx + 1} / ${quiz.size}"
        b.tvScore.text = "$score ✓"
        b.progressQuiz.progress = ((idx + 1) * 100 / quiz.size)

        val distractors = allWords.filter { it.id != cur.id }.shuffled().take(3)
        val choices = (distractors + cur).shuffled()
        val labels = listOf("A", "B", "C", "D")

        opts.forEachIndexed { i, btn ->
            btn.text = "${labels[i]}) ${choices[i].meaning}"
            btn.setBackgroundColor(Color.parseColor("#F2F2F7")) // indigo_light veya gray_bg[cite: 1]
            btn.isEnabled = true
            btn.setOnClickListener { check(btn, choices[i], cur) }
        }
    }

    private fun check(sel: Button, chosen: Word, correct: Word) {
        opts.forEach { it.isEnabled = false }

        if (chosen.id == correct.id) {
            sel.setBackgroundColor(Color.parseColor("#4CAF50"))
            vm.markCorrect(correct)
            score++
        } else {
            sel.setBackgroundColor(Color.parseColor("#F44336"))
            vm.markWrong(correct)
            opts.firstOrNull { it.text.contains(correct.meaning) }
                ?.setBackgroundColor(Color.parseColor("#4CAF50"))
        }

        Handler(Looper.getMainLooper()).postDelayed({
            idx++
            show()
        }, 1200)
    }
}