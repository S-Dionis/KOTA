package com.mwk.domain.api

import com.mwk.domain.model.Entry
import com.mwk.domain.model.EntryReminder

interface SaveEntryUseCase {

    suspend operator fun invoke(
        entry: Entry,
        reminders: List<EntryReminder>,
    )

}