package com.example.vocabtrainer.sync

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.vocabtrainer.data.local.AppDatabase
import com.example.vocabtrainer.data.local.Word
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Firestore <-> Room sync logic.
 *
 * Strategy:
 *  1. Fetch meta/version from Firestore.
 *  2. Compare with locally stored schemaVersion in SharedPreferences.
 *  3. If remote > local  →  bulk-fetch words collection, upsert into Room,
 *     then persist new version number.
 *  4. If equal           →  nothing to do, return early.
 *
 * Call [sync] from a WorkManager worker so it runs off the main thread
 * and is retried automatically on network failure.
 */
class SyncManager(private val context: Context) {

    companion object {
        private const val TAG = "SyncManager"
        private const val PREFS_NAME = "vocab_sync_prefs"
        private const val KEY_LOCAL_VERSION = "local_schema_version"

        // Firestore paths
        private const val COL_WORDS = "words"
        private const val COL_META = "meta"
        private const val DOC_VERSION = "version"
        private const val FIELD_SCHEMA_VERSION = "schemaVersion"
    }

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val db: AppDatabase by lazy { AppDatabase.get(context) }
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Main entry point.  Returns [SyncResult] so the caller (Worker) can
     * decide whether to retry or mark the job as succeeded.
     */
    suspend fun sync(): SyncResult {
        return try {
            val remoteVersion = fetchRemoteVersion()

            if (remoteVersion == null) {
                Log.w(TAG, "meta/version document not found – skipping sync")
                return SyncResult.Skipped("meta/version missing")
            }

            val localVersion = prefs.getInt(KEY_LOCAL_VERSION, 0)
            Log.d(TAG, "localVersion=$localVersion  remoteVersion=$remoteVersion")

            if (localVersion >= remoteVersion) {
                Log.d(TAG, "Already up to date")
                return SyncResult.UpToDate
            }

            val words = fetchAllWords()
            Log.d(TAG, "Fetched ${words.size} words from Firestore")

            upsertToRoom(words)
            saveLocalVersion(remoteVersion)

            Log.d(TAG, "Sync complete – upserted ${words.size} words, version=$remoteVersion")
            SyncResult.Success(wordsUpserted = words.size, newVersion = remoteVersion)

        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            SyncResult.Failure(e)
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private suspend fun fetchRemoteVersion(): Int? {
        val snapshot = firestore
            .collection(COL_META)
            .document(DOC_VERSION)
            .get()
            .await()

        return snapshot.getLong(FIELD_SCHEMA_VERSION)?.toInt()
    }

    private suspend fun fetchAllWords(): List<Word> {
        val snapshot = firestore
            .collection(COL_WORDS)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            runCatching { doc.toWord() }
                .onFailure { Log.w(TAG, "Skipping malformed doc ${doc.id}", it) }
                .getOrNull()
        }
    }

    private suspend fun upsertToRoom(words: List<Word>) {
        val dao = db.wordDao()
        // WordDao.insert uses OnConflictStrategy.REPLACE so this is a safe upsert.
        words.forEach { dao.insert(it) }
    }

    private fun saveLocalVersion(version: Int) {
        prefs.edit().putInt(KEY_LOCAL_VERSION, version).apply()
    }
}

// ── Firestore document → Word mapping ────────────────────────────────────────

/**
 * Maps a Firestore document to the local [Word] entity.
 *
 * Fields expected in each words/{docId} document:
 *   word            (String)  required
 *   meaning         (String)  required
 *   exampleSentence (String)  optional
 *   phonetic        (String)  optional
 *   audioUrl        (String)  optional
 *
 * NOTE: isLearned / correctCount / wrongCount / lastReviewDate are
 * intentionally NOT synced from Firestore – they are user-specific data
 * that lives only in Room.
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toWord(): Word {
    return Word(
        word            = getString("word")            ?: error("missing 'word'"),
        meaning         = getString("meaning")         ?: error("missing 'meaning'"),
        exampleSentence = getString("exampleSentence") ?: "",
        phonetic        = getString("phonetic")        ?: "",
        audioUrl        = getString("audioUrl")        ?: "",
        // Preserve user-specific fields at their Room defaults (not overwritten).
        isLearned       = false,
        correctCount    = 0,
        wrongCount      = 0,
        lastReviewDate  = 0L,
        addedDate       = System.currentTimeMillis()
    )
}

// ── Result ADT ───────────────────────────────────────────────────────────────

sealed class SyncResult {
    object UpToDate : SyncResult()
    data class Success(val wordsUpserted: Int, val newVersion: Int) : SyncResult()
    data class Skipped(val reason: String) : SyncResult()
    data class Failure(val error: Throwable) : SyncResult()
}