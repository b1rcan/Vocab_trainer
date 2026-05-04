package com.example.vocabtrainer.ui.profile
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.vocabtrainer.databinding.ActivityProfileBinding
import com.example.vocabtrainer.notification.NotificationHelper
import com.example.vocabtrainer.ui.WordViewModel
class ProfileActivity : AppCompatActivity() {
    private lateinit var b: ActivityProfileBinding
    private val vm: WordViewModel by viewModels()
    override fun onCreate(s: Bundle?) {
        super.onCreate(s); b = ActivityProfileBinding.inflate(layoutInflater); setContentView(b.root)
        vm.learned.observe(this) { words ->
            b.tvStatTotal.text = words.size.toString()
            val c = words.sumOf { it.correctCount }; val w = words.sumOf { it.wrongCount }
            b.tvStatAcc.text = if (c+w > 0) "%${c*100/(c+w)}" else "%0"
        }
        b.tvStatStreak.text = "12 gün"
        b.switchNotif.setOnCheckedChangeListener { _, on ->
            if (on) NotificationHelper.schedule(this) else NotificationHelper.cancel(this)
        }
    }
}
