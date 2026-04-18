package com.mwk.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReminderStopReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderDispatcher: ReminderDispatcher

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_STOP_REMINDER) return

        val notificationId = intent.getLongExtra(EXTRA_NOTIFICATION_ID, -1L)
        if (notificationId > 0L) {
            reminderDispatcher.stop(notificationId)
        }
    }

    companion object {
        const val ACTION_STOP_REMINDER = "com.mwk.data.reminder.STOP"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }
}
