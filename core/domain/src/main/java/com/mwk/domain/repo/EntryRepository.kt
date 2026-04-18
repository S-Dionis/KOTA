package com.mwk.domain.repo

import com.mwk.domain.model.Entry
import kotlinx.coroutines.flow.Flow

interface EntryRepository {
    suspend fun saveEntry(entry: Entry): Long
    fun observeEntries(startTimeMillis: Long, endTimeMillis: Long): Flow<List<Entry>>
    fun observeEntry(id: Long): Flow<Entry?>
    suspend fun setEntryEnabled(entryId: Long, isEnabled: Boolean)
}
