package com.mwk.data.reminder

data class ScheduledReminder(
    val notificationId: Long,
    val occurrenceStartMillis: Long,
    val triggerAtMillis: Long,
)
