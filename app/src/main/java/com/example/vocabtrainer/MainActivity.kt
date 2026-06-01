package com.example.vocabtrainer
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vocabtrainer.databinding.ActivityMainBinding
import com.example.vocabtrainer.notification.NotificationHelper
import com.example.vocabtrainer.ui.WordViewModel
import com.example.vocabtrainer.ui.profile.ProfileActivity
import com.example.vocabtrainer.ui.quiz.QuizActivity
import com.example.vocabtrainer.ui.review.ReviewActivity
import com.example.vocabtrainer.ui.wordlearning.WordLearningActivity
import com.example.vocabtrainer.ui.wordlist.WordAdapter
import com.example.vocabtrainer.ui.wordlist.WordListActivity
class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding
    private val vm: WordViewModel by viewModels()
    private val dailyGoal = 20
    override fun onCreate(s: Bundle?) {

        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        if (!prefs.getBoolean("synced", false)) {
            vm.syncFromFirebase()
            prefs.edit().putBoolean("synced", true).apply()
        }

        vm.syncFromFirebase()
        super.onCreate(s); b = ActivityMainBinding.inflate(layoutInflater); setContentView(b.root)
        NotificationHelper.createChannel(this); NotificationHelper.schedule(this)
        val adapter = WordAdapter {}
        b.rvRecentWords.layoutManager = LinearLayoutManager(this); b.rvRecentWords.adapter = adapter
        vm.learned.observe(this) { words ->
            adapter.submitList(words.take(5))
            val done = words.size.coerceAtMost(dailyGoal)
            b.tvProgress.text = "$done/$dailyGoal"
            b.progressDaily.progress = (done * 100 / dailyGoal).coerceAtMost(100)
            b.tvLearnedCount.text = words.size.toString()
            val c = words.sumOf { it.correctCount }; val w = words.sumOf { it.wrongCount }
            b.tvAccuracy.text = if (c+w>0) "%${c*100/(c+w)}" else "%0"
        }

        b.btnStart.setOnClickListener { startActivity(Intent(this, WordLearningActivity::class.java)) }
        b.navWords.setOnClickListener { startActivity(Intent(this, WordListActivity::class.java)) }
        b.navQuiz.setOnClickListener { startActivity(Intent(this, QuizActivity::class.java)) }
        b.navReview.setOnClickListener { startActivity(Intent(this, ReviewActivity::class.java)) }
        b.navProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        b.navHome.setOnClickListener { }
    }
}
