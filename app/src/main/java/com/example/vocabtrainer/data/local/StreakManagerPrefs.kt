package com.example.vocabtrainer.streak

import android.content.Context

/**
 * Quiz tamamlandığında çağrılan streak yöneticisi.
 *
 * Mantık:
 *  - Bugün zaten quiz tamamlandıysa streak değişmez.
 *  - Dün tamamlandıysa streak +1 artar (zincir devam ediyor).
 *  - 2+ gün ara verilmişse streak sıfırlanır, 1'den başlar.
 */
class StreakManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME       = "streak_prefs"
        private const val KEY_STREAK       = "streak_count"
        private const val KEY_LAST_DATE    = "last_quiz_date"   // "yyyy-MM-dd" formatında
    }

    /** Mevcut streak gün sayısını döner. */
    fun getStreak(): Int = prefs.getInt(KEY_STREAK, 0)

    /**
     * Quiz tamamlandığında çağır.
     * Bugün daha önce çağrıldıysa hiçbir şey yapmaz.
     * Yeni streak değerini döner.
     */
    fun recordQuizCompletion(): Int {
        val today     = todayString()
        val lastDate  = prefs.getString(KEY_LAST_DATE, null)

        // Bugün zaten tamamlandı
        if (lastDate == today) return getStreak()

        val newStreak = when (lastDate) {
            yesterdayString() -> getStreak() + 1   // zincir devam
            null              -> 1                  // ilk kez
            else              -> 1                  // zincir koptu, sıfırla
        }

        prefs.edit()
            .putInt(KEY_STREAK, newStreak)
            .putString(KEY_LAST_DATE, today)
            .apply()

        return newStreak
    }

    /** Streak'i tamamen sıfırla (test veya logout için). */
    fun reset() {
        prefs.edit().clear().apply()
    }

    // ── Tarih yardımcıları ────────────────────────────────────────────────────

    private fun todayString(): String = dateString(0)

    private fun yesterdayString(): String = dateString(-1)

    private fun dateString(offsetDays: Int): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, offsetDays)
        return String.format(
            "%04d-%02d-%02d",
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }
}