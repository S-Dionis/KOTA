package com.mwk.calendar.main

import com.mwk.calendar.entry.MainDispatcherRule
import com.mwk.kota.DateUtils
import com.mwk.domain.api.ObserveMainEntriesUseCase
import com.mwk.domain.model.Entry
import com.mwk.domain.model.EntryPeriod
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val zoneId: ZoneId = ZoneId.of("UTC")

    @Test
    fun `state entries by date for this month`() = runTest {
        val currentMonth = YearMonth.now()
        val entryDate = currentMonth.atDay(5)
        val entry = Entry(
            id = 1L,
            name = "Купить денег",
            startTimeMillis = DateUtils.getStartOfDayMillis(entryDate, zoneId) + 9 * 60 * 60 * 1000L,
            endTimeMillis = DateUtils.getStartOfDayMillis(entryDate, zoneId) + 10 * 60 * 60 * 1000L,
            isTask = true,
            isEnabled = false,
        )
        val observeMainEntriesUseCase = FakeObserveMainEntriesUseCase(listOf(entry))
        val viewModel = MainViewModel(
            observeMainEntriesUseCase = observeMainEntriesUseCase,
            zoneId = zoneId,
        )

        val collectJob = backgroundScope.launch { viewModel.state.collect { } }
        advanceUntilIdle()

        val entries = viewModel.state.value.entriesByDate[entryDate].orEmpty()

        assertEquals(1, entries.size)
        assertEquals("Купить денег", entries.single().title)
        assertTrue(entries.single().isTask)
        assertFalse(entries.single().isEnabled)
        collectJob.cancel()
    }

}

private class FakeObserveMainEntriesUseCase(
    private val entries: List<Entry>,
) : ObserveMainEntriesUseCase {

    val requestedPeriods = mutableListOf<EntryPeriod>()

    override fun invoke(period: EntryPeriod): Flow<List<Entry>> {
        requestedPeriods += period
        return flowOf(entries)
    }
}
