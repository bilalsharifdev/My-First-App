package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            DailyReminderManager.ACTION_DAILY_REMINDER -> {
                DailyReminderManager.showReminderNotification(context, isTest = false)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                // Re-schedule default 8:00 PM reminder after reboot
                DailyReminderManager.scheduleDailyReminder(
                    context = context,
                    enabled = true,
                    hourOfDay = 20,
                    minute = 0
                )
            }
        }
    }
}
