package com.mwk.domain.api

import com.mwk.domain.model.Entry
import kotlinx.coroutines.flow.Flow

interface ConsumeEntriesUseCase {

    operator fun invoke(startDayMillis: Long, endDayMillis: Long): Flow<List<Entry>>

}