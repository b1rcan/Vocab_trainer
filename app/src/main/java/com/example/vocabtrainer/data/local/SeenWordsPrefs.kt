package com.example.vocabtrainer.sync

import android.content.Context
const val DAILY_WORD_COUNT = 20
/**
 * Görülen kelime ID'lerini SharedPreferences'te tutar.
 *
 * Mantık:
 *  - [markSeen]  → ID'yi seen setine ekler.
 *  - [getUnseen] → kelime listesini filtreler; seen liste tükendiyse sıfırlar.
 *
 * Böylece kullanıcı tüm kelimeler tükenene kadar aynı kelimeyi görmez,
 * tükenince baştan başlar.
 */
class SeenWordsPrefs(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME  = "seen_words_prefs"
        private const val KEY_SEEN    = "seen_ids"
        private const val SEPARATOR   = ","
    }

    /** Verilen [ids] listesini seen olarak işaretle. */
    fun markSeen(ids: List<Int>) {
        val current = loadSeenIds().toMutableSet()
        current.addAll(ids.map { it.toString() })
        prefs.edit()
            .putString(KEY_SEEN, current.joinToString(SEPARATOR))
            .apply()
    }

    /**
     * [allWords] listesinden henüz gösterilmemiş kelimeleri döner.
     * Eğer kalan kelime [DAILY_WORD_COUNT]'tan azsa seen listesi sıfırlanır
     * ve tüm liste baştan kullanılır (sonsuz döngü garantisi).
     */
    fun <T : Identifiable> getUnseen(allWords: List<T>): List<T> {
        val seen = loadSeenIds()
        val unseen = allWords.filter { it.wordId.toString() !in seen }

        return if (unseen.size >= DAILY_WORD_COUNT) {
            unseen
        } else {
            // Tüm kelimeler görüldü → sıfırla ve baştan başla
            reset()
            allWords
        }
    }

    /** Seen listesini tamamen temizle. */
    fun reset() {
        prefs.edit().remove(KEY_SEEN).apply()
    }

    fun seenCount(): Int = loadSeenIds().size

    // ── Private ──────────────────────────────────────────────────────────────

    private fun loadSeenIds(): Set<String> {
        val raw = prefs.getString(KEY_SEEN, "") ?: ""
        return if (raw.isBlank()) emptySet()
        else raw.split(SEPARATOR).toSet()
    }
}

/** [SeenWordsPrefs] herhangi bir entity tipiyle çalışabilsin diye. */
interface Identifiable {
    val wordId: Int
}