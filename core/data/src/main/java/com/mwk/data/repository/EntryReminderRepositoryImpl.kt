package com.mwk.data.repository

import com.mwk.data.dao.EntryNotificationDao
import com.mwk.data.mapper.toDomain
import com.mwk.data.mapper.toEntity
import com.mwk.domain.model.EntryReminder
import com.mwk.domain.repo.EntryReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EntryReminderRepositoryImpl @Inject constructor(
    private val entryNotificationDao: EntryNotificationDao,
) : EntryReminderRepository {

    override suspend fun replaceReminders(entryId: Long, reminders: List<EntryReminder>) {
        entryNotificationDao.replaceNotifications(
            entryId = entryId,
            notifications = reminders.map { it.toEntity() },
        )
    }

    override fun observeReminders(entryId: Long): Flow<List<EntryReminder>> {
        return entryNotificationDao.observeNotifications(entryId)
            .map { notifications -> notifications.map { it.toDomain() } }
    }

    override suspend fun setEntryRemindersEnabled(entryId: Long, isEnabled: Boolean) {
        entryNotificationDao.updateEntryNotificationsEnabled(entryId, isEnabled)
    }
}
