package com.mwk.data.model

import androidx.room.ColumnInfo
import com.mwk.data.entity.EntryNotificationEntity

data class EntryReminderSchedule(
    @ColumnInfo(name = "notification_id")
    val notificationId: Long,
    @ColumnInfo(name = "notification_entryId")
    val notificationEntryId: Long,
    @ColumnInfo(name = "notification_remindBeforeMillis")
    val notificationRemindBeforeMillis: Long,
    @ColumnInfo(name = "notification_isEnabled")
    val notificationIsEnabled: Boolean,
    @ColumnInfo(name = "notification_lastTriggeredOccurrenceStartMillis")
    val notificationLastTriggeredOccurrenceStartMillis: Long?,
    @ColumnInfo(name = "entry_name")
    val entryName: String,
    @ColumnInfo(name = "entry_startTimeMillis")
    val entryStartTimeMillis: Long,
    @ColumnInfo(name = "entry_imagePath")
    val entryImagePath: String?,
)