package com.mwk.data.reminder

import com.mwk.data.dao.EntryNotificationDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderDispatcher @Inject constructor(
    private val entryNotificationDao: EntryNotificationDao,
    private val reminderNotificationFactory: ReminderNotificationFactory,
) {

    suspend fun dispatch(notificationId: Long, occurrenceStartMillis: Long) {
        val schedule = entryNotificationDao.getSchedule(notificationId) ?: return
        if (!schedule.notificationIsEnabled) return

        entryNotificationDao.updateLastTriggeredOccurrence(notificationId, occurrenceStartMillis)
        reminderNotificationFactory.show(schedule)
    }

    fun stop(notificationId: Long) {
        reminderNotificationFactory.stop(notificationId)
    }
}
