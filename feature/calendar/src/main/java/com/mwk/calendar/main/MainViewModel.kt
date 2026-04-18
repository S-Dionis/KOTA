package com.mwk.calendar.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mwk.kota.DateUtils
import com.mwk.domain.api.ObserveMainEntriesUseCase
import com.mwk.domain.model.Entry
import com.mwk.domain.model.EntryPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val observeMainEntriesUseCase: ObserveMainEntriesUseCase,
    private val zoneId: ZoneId,
) : ViewModel() {

    private val today = LocalDate.now()
    private val startFromMonth = YearMonth.from(today)
    private val currentOnScreenMonth = MutableStateFlow(startFromMonth)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val entriesByDate = currentOnScreenMonth
        .flatMapLatest { month ->
            val period = createEntriesPeriod(month)
            observeMainEntriesUseCase(period).map { entries ->
                createEntriesByDate(
                    entries = entries,
                    period = period,
                )
            }
        }

    val state = combine(
        entriesByDate,
        currentOnScreenMonth,
    ) { currentEntriesByDate, currentVisibleMonth ->
        MainCalendarState(
            today = today,
            startFromMonth = startFromMonth,
            currentOnScreenMonth = currentVisibleMonth,
            entriesByDate = currentEntriesByDate,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainCalendarState(
            today = today,
            startFromMonth = startFromMonth,
            currentOnScreenMonth = startFromMonth,
        )
    )

    fun onVisibleMonthChanged(month: YearMonth) {
        currentOnScreenMonth.update { month }
    }

    private fun createEntriesPeriod(month: YearMonth): EntryPeriod {
        val firstDayTwoMonthBefore = month.minusMonths(2).atDay(1)
        val lastDayTwoMonthAfter = month.plusMonths(3).atDay(1).minusDays(1)

        return EntryPeriod(
            startTimeMillis = DateUtils.getStartOfDayMillis(firstDayTwoMonthBefore, zoneId),
            endTimeMillis = DateUtils.getEndOfDayMillis(lastDayTwoMonthAfter, zoneId),
        )
    }

    private fun createEntriesByDate(
        entries: List<Entry>,
        period: EntryPeriod,
    ): Map<LocalDate, List<MainDayEntry>> {
        val visibleStartDate = DateUtils.getLocalDate(period.startTimeMillis, zoneId)
        val visibleEndDate = DateUtils.getLocalDate(period.endTimeMillis, zoneId)
        val result = linkedMapOf<LocalDate, MutableList<MainDayEntry>>()

        entries.forEach { entry ->
            val entryStartDate = DateUtils.getLocalDate(entry.startTimeMillis, zoneId)
            val lastVisibleInstant = (entry.endTimeMillis - 1L).coerceAtLeast(entry.startTimeMillis)
            val entryEndDate = DateUtils.getLocalDate(lastVisibleInstant, zoneId)
            val dayEntry = MainDayEntry(
                title = entry.name,
                isTask = entry.isTask,
                isEnabled = entry.isEnabled,
            )

            var currentDate = maxOf(entryStartDate, visibleStartDate)
            val lastDate = minOf(entryEndDate, visibleEndDate)

            while (!currentDate.isAfter(lastDate)) {
                result.getOrPut(currentDate) { mutableListOf() }.add(dayEntry)
                currentDate = currentDate.plusDays(1)
            }
        }

        return result
    }
}
