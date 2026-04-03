package io.github.nwma_fywf.mineword.data.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReviewScheduler {
    fun scheduleReviewReminder(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<ReviewReminderWorker>(
            1, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            ReviewReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun cancelReviewReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(ReviewReminderWorker.WORK_NAME)
    }
}
