package io.github.nwma_fywf.mineword.data.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            try {
                val prefs = context.getSharedPreferences("mineword_prefs", Context.MODE_PRIVATE)
                val enabled = prefs.getBoolean("review_reminder_enabled", false)
                if (enabled) {
                    ReviewScheduler.scheduleReviewReminder(context)
                }
            } catch (e: Exception) {
            }
        }
    }
}
