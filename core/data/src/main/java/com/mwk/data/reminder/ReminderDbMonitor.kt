package com.mwk.data.reminder

import com.mwk.data.dao.EntryNotificationDao
import com.mwk.data.model.EntryReminderSchedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderDbMonitor @Inject constructor(
    private val entryNotificationDao: EntryNotificationDao,
    private val reminderAlarmScheduler: ReminderAlarmScheduler,
) {

    private val started = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val knownIds = mutableSetOf<Long>()

    fun start() {
        if (!started.compareAndSet(false, true)) return

        scope.launch {
            entryNotificationDao.observeActiveSchedules().collectLatest { schedules ->
                val activeIds = schedules.map { it.notificationId }.toSet()

                schedules.forEach { schedule ->
                    val reminder = calculateNextReminder(schedule)
                    if (reminder == null) {
                        reminderAlarmScheduler.cancel(schedule.notificationId)
                    } else {
                        reminderAlarmScheduler.schedule(reminder)
                    }
                }

                knownIds.minus(activeIds).forEach(reminderAlarmScheduler::cancel)
                knownIds.clear()
                knownIds.addAll(activeIds)
            }
        }
    }


    private fun calculateNextReminder(
        schedule: EntryReminderSchedule,
        nowMillis: Long = System.currentTimeMillis(),
    ): ScheduledReminder? {
        val occurrenceStartMillis = nextOccurrenceStartMillis(schedule, nowMillis) ?: return null
        val triggerAtMillis = occurrenceStartMillis - schedule.notificationRemindBeforeMillis

        return ScheduledReminder(
            notificationId = schedule.notificationId,
            occurrenceStartMillis = occurrenceStartMillis,
            triggerAtMillis = triggerAtMillis,
        )
    }

    private fun nextOccurrenceStartMillis(
        schedule: EntryReminderSchedule,
        nowMillis: Long,
    ): Long? {
        val lastTriggered = schedule.notificationLastTriggeredOccurrenceStartMillis ?: Long.MIN_VALUE
        val triggerAtMillis = schedule.entryStartTimeMillis - schedule.notificationRemindBeforeMillis

        return if (schedule.entryStartTimeMillis > lastTriggered && triggerAtMillis > nowMillis) {
            schedule.entryStartTimeMillis
        } else {
            null
        }
    }

}
