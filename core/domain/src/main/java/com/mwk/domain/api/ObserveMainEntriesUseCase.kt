package com.mwk.domain.api

import com.mwk.domain.model.Entry
import com.mwk.domain.model.EntryPeriod
import kotlinx.coroutines.flow.Flow

interface ObserveMainEntriesUseCase {

    operator fun invoke(period: EntryPeriod): Flow<List<Entry>>

}