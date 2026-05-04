package com.example.vocabtrainer.ui.wordlist

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.*
import com.example.vocabtrainer.R
import com.example.vocabtrainer.data.local.Word
import com.example.vocabtrainer.sync.Identifiable
import com.example.vocabtrainer.sync.SeenWordsPrefs
const val DAILY_WORD_COUNT =20;
/**
 * [Word] için [ListAdapter].
 *
 * Firestore → Room → ViewModel akışı zaten LiveData üzerinden geliyor;
 * adapter sadece sunumu yapar.
 *
 * [SeenWordsPrefs] entegrasyonu ViewModel katmanında yapılır (bkz. WordViewModel),
 * adapter bunu bilmez — tek sorumluluğu listeyi render etmek.
 */
class WordAdapter(private val onClick: (Word) -> Unit) :
    ListAdapter<Word, WordAdapter.VH>(Diff()) {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvWord:    TextView = v.findViewById(R.id.tvWord)
        val tvMeaning: TextView = v.findViewById(R.id.tvMeaning)
        val tvBadge:   TextView = v.findViewById(R.id.tvBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_word, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val word = getItem(position)
        holder.tvWord.text    = word.word
        holder.tvMeaning.text = word.meaning
        holder.tvBadge.text   = if (word.isLearned) "Öğrenildi" else "Öğren"
        holder.itemView.setOnClickListener { onClick(word) }
    }

    class Diff : DiffUtil.ItemCallback<Word>() {
        override fun areItemsTheSame(oldItem: Word, newItem: Word) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Word, newItem: Word) = oldItem == newItem
    }
}

// ── Word → Identifiable bridge ────────────────────────────────────────────────

/**
 * [Word]'ü [SeenWordsPrefs.getUnseen] ile kullanabilmek için extension.
 * Word entity'sine dokunmadan interface'i karşılar.
 */
val Word.asIdentifiable: Identifiable
    get() = object : Identifiable {
        override val wordId: Int get() = this@asIdentifiable.id
    }

/**
 * Verilen listeden [DAILY_WORD_COUNT] kadar görülmemiş kelime döner.
 * Kullanım (ViewModel içinde):
 *
 *   val daily = allWords.pickUnseen(seenPrefs).shuffled()
 *   adapter.submitList(daily)
 *   seenPrefs.markSeen(daily.map { it.id })
 */
fun List<Word>.pickUnseen(prefs: SeenWordsPrefs): List<Word> {
    // Word listesini Identifiable wrapper'a çevir, unseen filtrele
    data class WordId(override val wordId: Int, val word: Word) : Identifiable

    val wrapped  = map { WordId(it.id, it) }
    val unseen   = prefs.getUnseen(wrapped)   // sıfırlama mantığı burada
    return unseen
        .shuffled()
        .take(DAILY_WORD_COUNT)
        .map { (it as WordId).word }
}