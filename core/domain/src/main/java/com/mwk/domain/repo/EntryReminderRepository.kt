package com.mwk.domain.repo

import com.mwk.domain.model.EntryReminder
import kotlinx.coroutines.flow.Flow

interface EntryReminderRepository {
    suspend fun replaceReminders(entryId: Long, reminders: List<EntryReminder>)
    fun observeReminders(entryId: Long): Flow<List<EntryReminder>>
    suspend fun setEntryRemindersEnabled(entryId: Long, isEnabled: Boolean)
}
