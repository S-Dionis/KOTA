package com.mwk.data.reminder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.mwk.data.R
import com.mwk.data.model.EntryReminderSchedule
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderNotificationFactory @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val notificationManager: NotificationManager
        get() = context.getSystemService(NotificationManager::class.java)

    fun show(schedule: EntryReminderSchedule) {
        ensureChannel()
        notificationManager.notify(
            schedule.notificationId.toInt(),
            createNotification(schedule),
        )
    }

    fun stop(notificationId: Long) {
        notificationManager.cancel(notificationId.toInt())
    }

    private fun createNotification(schedule: EntryReminderSchedule): Notification {
        val contentView = RemoteViews(context.packageName, R.layout.notification_entry_reminder).apply {
            setTextViewText(R.id.reminderTitleTextView, schedule.entryName)

            if (schedule.entryImagePath.isNullOrBlank()) {
                setViewVisibility(R.id.reminderImageView, View.INVISIBLE)
            } else {
                setViewVisibility(R.id.reminderImageView, View.VISIBLE)
                setImageViewUri(R.id.reminderImageView, Uri.fromFile(File(schedule.entryImagePath)))
            }

            setOnClickPendingIntent(
                R.id.reminderStopButton,
                PendingIntent.getBroadcast(
                    context,
                    schedule.notificationId.toInt(),
                    Intent(context, ReminderStopReceiver::class.java)
                        .setAction(ReminderStopReceiver.ACTION_STOP_REMINDER)
                        .putExtra(ReminderStopReceiver.EXTRA_NOTIFICATION_ID, schedule.notificationId),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(contentView)
            .setCustomBigContentView(contentView)
            .setAutoCancel(false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.reminder_channel_description)
        }

        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "entry_reminders"
    }
}
