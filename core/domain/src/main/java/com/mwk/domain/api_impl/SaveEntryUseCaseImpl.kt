package com.mwk.domain.api_impl

import com.mwk.domain.api.SaveEntryUseCase
import com.mwk.domain.model.Entry
import com.mwk.domain.model.EntryReminder
import com.mwk.domain.repo.EntryReminderRepository
import com.mwk.domain.repo.EntryRepository
import javax.inject.Inject

class SaveEntryUseCaseImpl @Inject constructor(
    private val entryRepository: EntryRepository,
    private val entryReminderRepository: EntryReminderRepository,
) : SaveEntryUseCase {

    override suspend operator fun invoke(
        entry: Entry,
        reminders: List<EntryReminder>,
    ) {
        val entryId = entryRepository.saveEntry(entry)
        entryReminderRepository.replaceReminders(
            entryId,
            reminders.map { reminder ->
                reminder.copy(entryId = entryId)
            }
        )
    }
}