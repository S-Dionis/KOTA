package com.mwk.data.repository

import com.mwk.data.dao.DayDao
import com.mwk.data.mapper.toDomain
import com.mwk.domain.repo.DayRepository
import com.mwk.domain.model.Entry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DayRepositoryImpl @Inject constructor(
    private val dayDao: DayDao,
) : DayRepository {

    override fun consumeDayEntries(startDayMillis: Long, endDayMillis: Long): Flow<List<Entry>> {
        return dayDao.observeTodayEntries(startDayMillis, endDayMillis)
            .map { entries -> entries.map { it.toDomain() } }
    }

}