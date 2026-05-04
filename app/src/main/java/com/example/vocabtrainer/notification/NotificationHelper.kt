package com.example.vocabtrainer.notification
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.vocabtrainer.MainActivity
import java.util.concurrent.TimeUnit
object NotificationHelper {
    private const val CH = "vocab_reminder"
    fun createChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CH, "Günlük Hatırlatma", NotificationManager.IMPORTANCE_DEFAULT)
            (ctx.getSystemService(NotificationManager::class.java)).createNotificationChannel(ch)
        }
    }
    fun show(ctx: Context) {
        val pi = PendingIntent.getActivity(ctx, 0, Intent(ctx, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(ctx, CH)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Bugünkü kelimelerin seni bekliyor!")
            .setContentText("Günlük hedefini tamamla.")
            .setContentIntent(pi).setAutoCancel(true).build()
        (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(1001, n)
    }
    fun schedule(ctx: Context) {
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
            "vocab_daily", ExistingPeriodicWorkPolicy.REPLACE,
            PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS).build()
        )
    }
    fun cancel(ctx: Context) = WorkManager.getInstance(ctx).cancelUniqueWork("vocab_daily")
}
