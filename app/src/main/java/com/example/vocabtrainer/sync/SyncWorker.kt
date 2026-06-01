package com.example.vocabtrainer.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * WorkManager worker that delegates all sync logic to [SyncManager].
 *
 * Retry policy (set in [SyncScheduler]):
 *   - Exponential back-off, network constraint required.
 *   - Returns [Result.retry] on any [SyncResult.Failure] so WorkManager
 *     automatically retries when connectivity is restored.
 */
class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "SyncWorker"
        const val WORK_NAME = "vocab_rtdb_sync"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork() started  attempt=${runAttemptCount + 1}")

        return when (val result = SyncManager(applicationContext).sync()) {
            is SyncResult.UpToDate -> {
                Log.d(TAG, "Nothing to sync")
                Result.success()
            }
            is SyncResult.Success -> {
                Log.d(TAG, "Synced ${result.wordsUpserted} words → v${result.newVersion}")
                Result.success()
            }
            is SyncResult.Skipped -> {
                Log.w(TAG, "Sync skipped: ${result.reason}")
                Result.success()           // don't retry for a missing doc
            }
            is SyncResult.Failure -> {
                Log.e(TAG, "Sync failed on attempt ${runAttemptCount + 1}", result.error)
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        }
    }
}