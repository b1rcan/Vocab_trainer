package com.example.vocabtrainer.ui.wordlist
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vocabtrainer.databinding.ActivityWordListBinding
import com.example.vocabtrainer.ui.WordViewModel
class WordListActivity : AppCompatActivity() {
    private lateinit var b: ActivityWordListBinding
    private val vm: WordViewModel by viewModels()
    override fun onCreate(s: Bundle?) {
        super.onCreate(s); b = ActivityWordListBinding.inflate(layoutInflater); setContentView(b.root)
        val adapter = WordAdapter {}
        b.rvWords.layoutManager = LinearLayoutManager(this); b.rvWords.adapter = adapter
        vm.all.observe(this) { adapter.submitList(it) }
        b.etSearch.addTextChangedListener { t ->
            val q = t.toString().trim()
            if (q.isEmpty()) vm.all.observe(this) { adapter.submitList(it) }
            else { vm.setQuery(q); vm.searchResults.observe(this) { adapter.submitList(it) } }
        }
    }
}
