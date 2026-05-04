package com.example.vocabtrainer

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vocabtrainer.databinding.ActivityMainBinding
import com.example.vocabtrainer.notification.NotificationHelper
import com.example.vocabtrainer.streak.StreakManager
import com.example.vocabtrainer.sync.SyncScheduler
import com.example.vocabtrainer.ui.WordViewModel
import com.example.vocabtrainer.ui.profile.ProfileActivity
import com.example.vocabtrainer.ui.quiz.QuizActivity
import com.example.vocabtrainer.ui.review.ReviewActivity
import com.example.vocabtrainer.ui.wordlearning.WordLearningActivity
import com.example.vocabtrainer.ui.wordlist.WordAdapter
import com.example.vocabtrainer.ui.wordlist.WordListActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var streakManager: StreakManager
    private val vm: WordViewModel by viewModels()
    private val dailyGoal = 20

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        auth = FirebaseAuth.getInstance()
        streakManager = StreakManager(this)

        NotificationHelper.createChannel(this)
        NotificationHelper.schedule(this)

        SyncScheduler.enqueueOneTime(this)
        SyncScheduler.enqueueRecurring(this)

        // Kullanıcı selamlama
        auth.currentUser?.let { user ->
            b.tvGreeting.text = "Merhaba, ${user.displayName ?: "Kullanıcı"} :)"
        }

        // RecyclerView
        val adapter = WordAdapter {}
        b.rvRecentWords.layoutManager = LinearLayoutManager(this)
        b.rvRecentWords.adapter = adapter

        // İstatistikler
        vm.learned.observe(this) { words ->
            adapter.submitList(words.take(5))
            val done = words.size.coerceAtMost(dailyGoal)
            b.tvProgress.text = "$done/$dailyGoal"
            b.progressDaily.progress = (done * 100 / dailyGoal).coerceAtMost(100)
            b.tvLearnedCount.text = words.size.toString()
            val c = words.sumOf { it.correctCount }
            val w = words.sumOf { it.wrongCount }
            b.tvAccuracy.text = if (c + w > 0) "%${c * 100 / (c + w)}" else "%0"
        }

        // Streak — SharedPrefs'ten okunur, quiz bitince güncellenir
        b.tvStreak.text = streakManager.getStreak().toString()

        // Navigasyon
        b.btnStart.setOnClickListener {
            startActivity(Intent(this, WordLearningActivity::class.java).putExtra("mode", "daily"))
        }
        b.navWords.setOnClickListener   { startActivity(Intent(this, WordListActivity::class.java)) }
        b.navQuiz.setOnClickListener    { startActivity(Intent(this, QuizActivity::class.java)) }
        b.navReview.setOnClickListener  { startActivity(Intent(this, ReviewActivity::class.java)) }
        b.navProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        b.navHome.setOnClickListener    { }
    }

    // Quiz'den dönünce streak güncellenmiş olabilir, yenile
    override fun onResume() {
        super.onResume()
        b.tvStreak.text = streakManager.getStreak().toString()
    }
}