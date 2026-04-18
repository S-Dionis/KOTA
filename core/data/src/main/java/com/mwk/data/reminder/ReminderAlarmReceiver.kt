package com.mwk.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderDispatcher: ReminderDispatcher

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TRIGGER_REMINDER) return

        val pendingResult = goAsync()
        val notificationId = intent.getLongExtra(EXTRA_NOTIFICATION_ID, -1L)
        val occurrenceStartMillis = intent.getLongExtra(EXTRA_OCCURRENCE_START_MILLIS, -1L)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (notificationId > 0L && occurrenceStartMillis > 0L) {
                    reminderDispatcher.dispatch(notificationId, occurrenceStartMillis)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_TRIGGER_REMINDER = "com.mwk.data.reminder.TRIGGER"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_OCCURRENCE_START_MILLIS = "occurrence_start_millis"
    }
}
