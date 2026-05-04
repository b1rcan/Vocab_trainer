package com.example.vocabtrainer.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Central place to enqueue / cancel sync jobs.
 *
 * Usage:
 *   // App startup – run once immediately if needed:
 *   SyncScheduler.enqueueOneTime(context)
 *
 *   // Also schedule a background check every 24 h:
 *   SyncScheduler.enqueueRecurring(context)
 */
object SyncScheduler {

    private val networkConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /**
     * One-time sync. Safe to call every app launch – WorkManager deduplicates
     * via [ExistingWorkPolicy.KEEP] so if a job is already queued it won't
     * be enqueued again.
     */
    fun enqueueOneTime(context: Context) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(networkConstraint)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30, TimeUnit.SECONDS          // initial back-off 30 s
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                SyncWorker.WORK_NAME + "_once",
                ExistingWorkPolicy.KEEP,       // don't restart if already queued
                request
            )
    }

    /**
     * Periodic background sync – checks for new content every 24 hours.
     * Uses [ExistingPeriodicWorkPolicy.UPDATE] so an already-scheduled job
     * is updated with the latest constraints if you call this again.
     */
    fun enqueueRecurring(context: Context) {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(24, TimeUnit.HOURS)
            .setConstraints(networkConstraint)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30, TimeUnit.SECONDS
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                SyncWorker.WORK_NAME + "_periodic",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
    }

    /** Cancel all pending sync jobs (e.g. on logout). */
    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).apply {
            cancelUniqueWork(SyncWorker.WORK_NAME + "_once")
            cancelUniqueWork(SyncWorker.WORK_NAME + "_periodic")
        }
    }
}