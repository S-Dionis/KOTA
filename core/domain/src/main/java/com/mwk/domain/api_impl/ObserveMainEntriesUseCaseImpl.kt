package com.mwk.domain.api_impl

import com.mwk.domain.api.ObserveMainEntriesUseCase
import com.mwk.domain.model.Entry
import com.mwk.domain.model.EntryPeriod
import com.mwk.domain.repo.EntryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMainEntriesUseCaseImpl @Inject constructor(
    val entryRepository: EntryRepository
): ObserveMainEntriesUseCase {

    override fun invoke(period: EntryPeriod): Flow<List<Entry>> {
        return entryRepository.observeEntries(
            startTimeMillis = period.startTimeMillis,
            endTimeMillis = period.endTimeMillis,
        )
    }

}