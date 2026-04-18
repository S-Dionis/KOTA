package com.mwk.domain.api_impl


import com.mwk.domain.repo.DayRepository
import com.mwk.domain.model.Entry
import com.mwk.domain.api.ConsumeEntriesUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ConsumeEntriesUseCaseImpl @Inject constructor(
    private val dayRepository: DayRepository
): ConsumeEntriesUseCase {

    override operator fun invoke(startDayMillis: Long, endDayMillis: Long): Flow<List<Entry>> {
        return dayRepository.consumeDayEntries(startDayMillis, endDayMillis)
    }

}