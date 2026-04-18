package com.mwk.data.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val alarmManager: AlarmManager
        get() = context.getSystemService(AlarmManager::class.java)

    fun schedule(reminder: ScheduledReminder) {
        val pendingIntent = createPendingIntent(
            notificationId = reminder.notificationId,
            occurrenceStartMillis = reminder.occurrenceStartMillis,
        )

        if (canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.triggerAtMillis,
                pendingIntent,
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.triggerAtMillis,
                pendingIntent,
            )
        }
    }

    fun cancel(notificationId: Long) {
        val pendingIntent = createPendingIntentOrNull(
            notificationId = notificationId,
        ) ?: return

        alarmManager.cancel(pendingIntent)
    }

    private fun createPendingIntent(
        notificationId: Long,
        occurrenceStartMillis: Long,
    ): PendingIntent {
        val intent = createReminderIntent(notificationId, occurrenceStartMillis)

        return PendingIntent.getBroadcast(
            context,
            notificationId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createPendingIntentOrNull(
        notificationId: Long,
    ): PendingIntent? {
        val intent = createReminderIntent(notificationId, 0L)

        return PendingIntent.getBroadcast(
            context,
            notificationId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createReminderIntent(
        notificationId: Long,
        occurrenceStartMillis: Long,
    ): Intent {
        return Intent(context, ReminderAlarmReceiver::class.java)
            .setAction(ReminderAlarmReceiver.ACTION_TRIGGER_REMINDER)
            .putExtra(ReminderAlarmReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            .putExtra(ReminderAlarmReceiver.EXTRA_OCCURRENCE_START_MILLIS, occurrenceStartMillis)
    }

    private fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
}
