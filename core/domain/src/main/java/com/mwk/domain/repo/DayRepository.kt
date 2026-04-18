package com.mwk.domain.repo

import com.mwk.domain.model.Entry
import kotlinx.coroutines.flow.Flow

interface DayRepository {

    fun consumeDayEntries(startDayMillis: Long, endDayMillis: Long): Flow<List<Entry>>

}