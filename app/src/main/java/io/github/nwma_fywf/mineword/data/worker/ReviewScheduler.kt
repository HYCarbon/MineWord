package io.github.nwma_fywf.mineword.data.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReviewScheduler {
    private const val TAG = "ReviewScheduler"

    fun scheduleReviewReminder(context: Context, hour: Int = 20, minute: Int = 0) {
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val delayMinutes: Long
        if (targetTime.after(currentTime)) {
            delayMinutes = (targetTime.timeInMillis - currentTime.timeInMillis) / 1000 / 60
        } else {
            targetTime.add(Calendar.DAY_OF_MONTH, 1)
            delayMinutes = (targetTime.timeInMillis - currentTime.timeInMillis) / 1000 / 60
        }

        Log.d(TAG, "Scheduling reminder in $delayMinutes minutes at ${hour}:${minute}")

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<ReviewReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .addTag("review_reminder")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            ReviewReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelReviewReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(ReviewReminderWorker.WORK_NAME)
    }
}
