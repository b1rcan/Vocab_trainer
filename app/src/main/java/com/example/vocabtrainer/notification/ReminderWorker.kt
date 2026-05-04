package com.example.vocabtrainer.notification
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
class ReminderWorker(ctx: Context, p: WorkerParameters) : Worker(ctx, p) {
    override fun doWork(): Result { NotificationHelper.show(applicationContext); return Result.success() }
}
