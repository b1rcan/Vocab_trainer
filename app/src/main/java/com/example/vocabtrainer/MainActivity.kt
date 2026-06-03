package com.example.vocabtrainer

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
import com.example.vocabtrainer.databinding.ActivityMainBinding
import com.example.vocabtrainer.data.local.UserNamePrefs
import com.example.vocabtrainer.notification.NotificationHelper
import com.example.vocabtrainer.streak.StreakManager
import com.example.vocabtrainer.sync.SyncManager
import com.example.vocabtrainer.sync.SyncScheduler
import com.example.vocabtrainer.ui.WordViewModel
import com.example.vocabtrainer.ui.profile.ProfileActivity
import com.example.vocabtrainer.ui.quiz.QuizActivity
import com.example.vocabtrainer.ui.review.ReviewActivity
import com.example.vocabtrainer.ui.wordlearning.WordLearningActivity
import com.example.vocabtrainer.ui.wordlist.WordAdapter
import com.example.vocabtrainer.ui.wordlist.WordListActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var streakManager: StreakManager
    private lateinit var userNamePrefs: UserNamePrefs
    private val vm: WordViewModel by viewModels()
    private val dailyGoal = 20

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        auth = FirebaseAuth.getInstance()
        streakManager = StreakManager(this)
        userNamePrefs = UserNamePrefs(this)

        NotificationHelper.createChannel(this)
        NotificationHelper.schedule(this)

        lifecycleScope.launch(Dispatchers.IO) {
            SyncManager(applicationContext).sync()
        }
        SyncScheduler.enqueueOneTime(this)
        SyncScheduler.enqueueRecurring(this)

        auth.currentUser?.let { user ->
            val displayName = user.displayName?.trim().takeUnless { it.isNullOrEmpty() }
                ?: userNamePrefs.getName()
                ?: user.email?.substringBefore("@")
                ?: getString(R.string.profile_default_name)
            userNamePrefs.saveName(displayName)
            b.tvGreeting.text = getString(
                R.string.greeting_user,
                displayName
            )
        }

        val adapter = WordAdapter {}
        b.rvRecentWords.layoutManager = LinearLayoutManager(this)
        b.rvRecentWords.adapter = adapter

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

        b.tvDailyGoal.text = getString(R.string.daily_goal, dailyGoal)
        b.tvStreak.text = streakManager.getStreak().toString()

        b.btnStart.setOnClickListener {
            startActivity(Intent(this, WordLearningActivity::class.java).putExtra("mode", "daily"))
        }
        b.navWords.setOnClickListener   { startActivity(Intent(this, WordListActivity::class.java)) }
        b.navQuiz.setOnClickListener    { startActivity(Intent(this, QuizActivity::class.java)) }
        b.navReview.setOnClickListener  { startActivity(Intent(this, ReviewActivity::class.java)) }
        b.navProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        b.navHome.setOnClickListener    { }
    }

    override fun onResume() {
        super.onResume()
        b.tvStreak.text = streakManager.getStreak().toString()
    }
}