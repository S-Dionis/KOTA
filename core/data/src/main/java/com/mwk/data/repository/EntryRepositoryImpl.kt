package com.mwk.data.repository

import com.mwk.data.dao.EntryDao
import com.mwk.data.mapper.toEntity
import com.mwk.data.mapper.toDomain
import com.mwk.domain.model.Entry
import com.mwk.domain.repo.EntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EntryRepositoryImpl @Inject constructor(
    private val entryDao: EntryDao,
) : EntryRepository {

    override suspend fun saveEntry(entry: Entry): Long {
        return entryDao.insertEntry(entry.toEntity())
    }

    override fun observeEntries(startTimeMillis: Long, endTimeMillis: Long): Flow<List<Entry>> {
        return entryDao.observeEntries(
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
        ).map { entries ->
            entries.map { it.toDomain() }
        }
    }

    override fun observeEntry(id: Long): Flow<Entry?> {
        return entryDao.selectEntry(id)
            .map { entry -> entry?.toDomain() }
    }

    override suspend fun setEntryEnabled(entryId: Long, isEnabled: Boolean) {
        entryDao.updateEntryEnabled(entryId, isEnabled)
    }

}
