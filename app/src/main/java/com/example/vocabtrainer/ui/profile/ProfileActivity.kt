package com.example.vocabtrainer.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.vocabtrainer.R
import com.example.vocabtrainer.LoginActivity
import com.example.vocabtrainer.data.local.UserNamePrefs
import com.example.vocabtrainer.databinding.ActivityProfileBinding
import com.example.vocabtrainer.notification.NotificationHelper
import com.example.vocabtrainer.streak.StreakManager
import com.example.vocabtrainer.ui.WordViewModel
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private lateinit var b: ActivityProfileBinding
    private val vm: WordViewModel by viewModels()
    private lateinit var streakManager: StreakManager
    private lateinit var userNamePrefs: UserNamePrefs
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(b.root)

        streakManager = StreakManager(this)
        userNamePrefs = UserNamePrefs(this)

        val displayName = auth.currentUser?.displayName?.trim().takeUnless { it.isNullOrEmpty() }
            ?: userNamePrefs.getName()
            ?: auth.currentUser?.email?.substringBefore("@")
            ?: getString(R.string.profile_default_name)
        userNamePrefs.saveName(displayName)
        b.tvName.text = displayName
        b.tvAvatarInitial.text = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

        // Toplam kelime sayısı
        vm.all.observe(this) { words ->
            b.tvStatTotal.text = words.size.toString()
        }

        // Öğrenilen kelime sayısı + doğruluk oranı
        vm.learned.observe(this) { words ->
            b.tvStatLearned.text = words.size.toString()
            val c = words.sumOf { it.correctCount }
            val w = words.sumOf { it.wrongCount }
            b.tvStatAcc.text = if (c + w > 0) "%${c * 100 / (c + w)}" else "%0"
        }

        // Streak — SharedPrefs'ten okunur, quiz bitince güncellenir
        b.tvStatStreak.text = "${streakManager.getStreak()} gün streak"

        b.switchNotif.setOnCheckedChangeListener { _, on ->
            if (on) NotificationHelper.schedule(this)
            else NotificationHelper.cancel(this)
        }

        b.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}