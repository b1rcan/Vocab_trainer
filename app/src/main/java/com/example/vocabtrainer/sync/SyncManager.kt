package com.example.vocabtrainer.sync

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.vocabtrainer.data.local.AppDatabase
import com.example.vocabtrainer.data.local.Word
import com.example.vocabtrainer.data.remote.FirebaseRealtimeDatabaseClient
import com.google.firebase.database.DataSnapshot
import kotlinx.coroutines.tasks.await

/**
 * Firebase Realtime Database <-> Room sync logic.
 *
 * Strategy:
 *  1. Fetch meta/version from Realtime Database.
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

        // Realtime Database paths
        // NOTE: Words are stored at root level with numeric keys ("0", "1", ..., "100").
        //       "meta" and "users" are non-word nodes and must be excluded.
        private const val COL_META = "meta"
        private const val DOC_VERSION = "version"
        private const val FIELD_SCHEMA_VERSION = "schemaVersion"

        /** Node keys at root level that are NOT word entries. */
        private val EXCLUDED_ROOT_KEYS = setOf("meta", "users")
    }

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
                Log.w(TAG, "meta/version node not found – skipping sync")
                return SyncResult.Skipped("meta/version missing")
            }

            val localVersion = prefs.getInt(KEY_LOCAL_VERSION, 0)
            Log.d(TAG, "localVersion=$localVersion  remoteVersion=$remoteVersion")

            // Eğer Room'da hiç kelime yoksa, eski bir bug nedeniyle version kaydedilmiş
            // ama kelimeler yazılmamış olabilir. Bu durumda her zaman sync yap.
            val roomWordCount = db.wordDao().countSync()
            val forceSync = roomWordCount == 0
            Log.d(TAG, "roomWordCount=$roomWordCount  forceSync=$forceSync")

            if (localVersion >= remoteVersion && !forceSync) {
                Log.d(TAG, "Already up to date")
                return SyncResult.UpToDate
            }

            val words = fetchAllWords()
            Log.d(TAG, "Fetched ${words.size} words from Realtime Database")

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
        val snapshot = FirebaseRealtimeDatabaseClient
            .root
            .child(COL_META)
            .child(DOC_VERSION)
            .get()
            .await()

        Log.d(TAG, "meta/version raw value: ${snapshot.value}  exists=${snapshot.exists()}")

        // Firebase'de meta/version direkt bir sayı olarak saklanabilir (örn: 1)
        // ya da bir obje olarak { schemaVersion: 1 } şeklinde olabilir.
        // Her iki durumu da destekliyoruz:
        val direct = snapshot.getValue(Long::class.java)?.toInt()
        if (direct != null) return direct

        val nested = snapshot.child(FIELD_SCHEMA_VERSION).getValue(Long::class.java)?.toInt()
        if (nested != null) return nested

        Log.w(TAG, "meta/version okunamadı. snapshot.value=${snapshot.value}")
        return null
    }

    private suspend fun fetchAllWords(): List<Word> {
        // Words are stored at Firebase root level with numeric keys ("0", "1", etc.).
        // Non-word nodes such as "meta" and "users" are excluded by key.
        val snapshot = FirebaseRealtimeDatabaseClient
            .root
            .get()
            .await()

        Log.d(TAG, "Root snapshot children count: ${snapshot.childrenCount}")

        return snapshot.children
            .filter { child -> child.key !in EXCLUDED_ROOT_KEYS }
            .mapNotNull { child ->
                runCatching { child.toWord() }
                    .onFailure { Log.w(TAG, "Skipping malformed node ${child.key}", it) }
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

// ── Realtime Database node → Word mapping ────────────────────────────────────

/**
 * Maps a Realtime Database node to the local [Word] entity.
 *
 * Fields expected in each words/{nodeId} node:
 *   word            (String)  required
 *   meaning         (String)  required
 *   exampleSentence (String)  optional
 *   phonetic        (String)  optional
 *   audioUrl        (String)  optional
 *
 * NOTE: isLearned / correctCount / wrongCount / lastReviewDate are
 * intentionally NOT synced from Realtime Database – they are user-specific data
 * that lives only in Room.
 */
private fun DataSnapshot.toWord(): Word {
    return Word(
        word            = child("word").getValue(String::class.java) ?: error("missing 'word'"),
        meaning         = child("meaning").getValue(String::class.java) ?: error("missing 'meaning'"),
        exampleSentence = child("exampleSentence").getValue(String::class.java) ?: "",
        phonetic        = child("phonetic").getValue(String::class.java) ?: "",
        audioUrl        = child("audioUrl").getValue(String::class.java) ?: "",
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