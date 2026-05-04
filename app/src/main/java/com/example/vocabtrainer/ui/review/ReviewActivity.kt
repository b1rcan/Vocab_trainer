package com.example.vocabtrainer.ui.review
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.vocabtrainer.databinding.ActivityReviewBinding
import com.example.vocabtrainer.ui.WordViewModel
import com.example.vocabtrainer.ui.wordlearning.WordLearningActivity
class ReviewActivity : AppCompatActivity() {
    private lateinit var b: ActivityReviewBinding
    private val vm: WordViewModel by viewModels()
    override fun onCreate(s: Bundle?) {
        super.onCreate(s); b = ActivityReviewBinding.inflate(layoutInflater); setContentView(b.root)
        vm.all.observe(this) { b.tvAllCount.text = "${it.size} kelime" }
        vm.weak.observe(this) { b.tvWeakCount.text = "${it.size} kelime" }
        b.btnRevAll.setOnClickListener { startActivity(Intent(this, WordLearningActivity::class.java).putExtra("mode", "all")) }
        b.btnRevWeak.setOnClickListener { startActivity(Intent(this, WordLearningActivity::class.java).putExtra("mode","weak")) }
        b.btnRevDaily.setOnClickListener { startActivity(Intent(this, WordLearningActivity::class.java).putExtra("mode","daily")) }
    }
}
