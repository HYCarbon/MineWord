package io.github.nwma_fywf.mineword.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.nwma_fywf.mineword.MainActivity
import io.github.nwma_fywf.mineword.MineWordApplication
import io.github.nwma_fywf.mineword.R
import io.github.nwma_fywf.mineword.data.local.WordDatabase

class ReviewReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = WordDatabase.getDatabase(context)
            val wordDao = database.wordDao()
            val currentTime = System.currentTimeMillis()
            val dueCount = wordDao.getDueReviewCount(currentTime)

            if (dueCount > 0) {
                showNotification(dueCount)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showNotification(count: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "复习提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "基于艾宾浩斯遗忘曲线的单词复习提醒"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("复习提醒")
            .setContentText("您有 $count 个单词需要复习")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val WORK_NAME = "review_reminder_work"
        const val CHANNEL_ID = "review_reminder_channel"
        const val NOTIFICATION_ID = 1001
    }
}
