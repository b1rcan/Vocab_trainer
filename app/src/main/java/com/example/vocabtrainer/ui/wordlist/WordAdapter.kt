package com.example.vocabtrainer.ui.wordlist
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.*
import com.example.vocabtrainer.R
import com.example.vocabtrainer.data.local.Word
class WordAdapter(private val onClick: (Word) -> Unit) : ListAdapter<Word, WordAdapter.VH>(Diff()) {
    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvWord: TextView = v.findViewById(R.id.tvWord)
        val tvMeaning: TextView = v.findViewById(R.id.tvMeaning)
        val tvBadge: TextView = v.findViewById(R.id.tvBadge)
    }
    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(LayoutInflater.from(p.context).inflate(R.layout.item_word, p, false))
    override fun onBindViewHolder(h: VH, pos: Int) {
        val w = getItem(pos)
        h.tvWord.text = w.word; h.tvMeaning.text = w.meaning
        h.tvBadge.text = if (w.isLearned) "✓ Öğrenildi" else "📖 Öğren"
        h.itemView.setOnClickListener { onClick(w) }
    }
    class Diff : DiffUtil.ItemCallback<Word>() {
        override fun areItemsTheSame(a: Word, b: Word) = a.id == b.id
        override fun areContentsTheSame(a: Word, b: Word) = a == b
    }
}
