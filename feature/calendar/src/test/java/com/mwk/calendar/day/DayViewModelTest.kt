package com.mwk.calendar.day

import com.mwk.calendar.entry.MainDispatcherRule
import com.mwk.kota.DateUtils
import com.mwk.domain.api.ConsumeEntriesUseCase
import com.mwk.domain.model.Entry
import com.mwk.domain.model.EntryReminder
import com.mwk.domain.repo.EntryReminderRepository
import com.mwk.domain.repo.EntryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class DayViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val zoneId: ZoneId = ZoneId.of("UTC")

    @Test
    fun `entries to ui state`() = runTest {
        val date = LocalDate.of(2026, 4, 17)
        val entry = Entry(
            id = 7L,
            name = "Трогать кота",
            startTimeMillis = DateUtils.getStartOfDayMillis(date, zoneId) + 8 * 60 * 60 * 1000L,
            endTimeMillis = DateUtils.getStartOfDayMillis(date, zoneId) + 9 * 60 * 60 * 1000L,
            isTask = true,
            isEnabled = true,
            imagePath = null,
        )
        val consumeEntriesUseCase = FakeConsumeEntriesUseCase(listOf(entry))
        val viewModel = DayViewModel(
            consumeEntriesUseCase = consumeEntriesUseCase,
            entryRepository = FakeDayEntryRepository(),
            entryReminderRepository = FakeDayEntryReminderRepository(),
            zoneId = zoneId,
            initialDate = date,
        )

        advanceUntilIdle()

        val task = viewModel.state.value.tasks.single()
        val expectedStart = DateUtils.getStartOfDayMillis(date, zoneId)
        val expectedEnd = DateUtils.getEndOfDayMillis(date, zoneId) + 1

        assertEquals(expectedStart, consumeEntriesUseCase.requestedRanges.single().first)
        assertEquals(expectedEnd, consumeEntriesUseCase.requestedRanges.single().second)
        assertEquals(7L, task.id)
        assertEquals("Трогать кота", task.title)
        assertEquals("08:00", task.startTime)
        assertEquals("09:00", task.endTime)
    }

    @Test
    fun `onTaskCheckedChange disables entry and reminders when checked`() = runTest {
        val entryRepository = FakeDayEntryRepository()
        val reminderRepository = FakeDayEntryReminderRepository()
        val viewModel = DayViewModel(
            consumeEntriesUseCase = FakeConsumeEntriesUseCase(emptyList()),
            entryRepository = entryRepository,
            entryReminderRepository = reminderRepository,
            zoneId = zoneId,
            initialDate = LocalDate.of(2026, 4, 17),
        )

        viewModel.onTaskCheckedChange(taskId = 12L, isChecked = true)
        advanceUntilIdle()

        assertEquals(12L to false, entryRepository.enabledChanges.single())
        assertEquals(12L to false, reminderRepository.enabledChanges.single())
    }

    @Test
    fun `onTaskCheckedChange enables entry and reminders when unchecked`() = runTest {
        val entryRepository = FakeDayEntryRepository()
        val reminderRepository = FakeDayEntryReminderRepository()
        val viewModel = DayViewModel(
            consumeEntriesUseCase = FakeConsumeEntriesUseCase(emptyList()),
            entryRepository = entryRepository,
            entryReminderRepository = reminderRepository,
            zoneId = zoneId,
            initialDate = LocalDate.of(2026, 4, 17),
        )

        viewModel.onTaskCheckedChange(taskId = 12L, isChecked = false)
        advanceUntilIdle()

        assertEquals(12L to true, entryRepository.enabledChanges.single())
        assertEquals(12L to true, reminderRepository.enabledChanges.single())
    }
}

private class FakeConsumeEntriesUseCase(
    entries: List<Entry>,
) : ConsumeEntriesUseCase {

    val requestedRanges = mutableListOf<Pair<Long, Long>>()
    private val flow = MutableStateFlow(entries)

    override fun invoke(startDayMillis: Long, endDayMillis: Long): Flow<List<Entry>> {
        requestedRanges += startDayMillis to endDayMillis
        return flow
    }
}

private class FakeDayEntryRepository : EntryRepository {
    val enabledChanges = mutableListOf<Pair<Long, Boolean>>()

    override suspend fun saveEntry(entry: Entry): Long = entry.id

    override fun observeEntries(startTimeMillis: Long, endTimeMillis: Long): Flow<List<Entry>> = emptyFlow()

    override fun observeEntry(id: Long): Flow<Entry?> = emptyFlow()

    override suspend fun setEntryEnabled(entryId: Long, isEnabled: Boolean) {
        enabledChanges += entryId to isEnabled
    }
}

private class FakeDayEntryReminderRepository : EntryReminderRepository {
    val enabledChanges = mutableListOf<Pair<Long, Boolean>>()

    override suspend fun replaceReminders(entryId: Long, reminders: List<EntryReminder>) = Unit

    override fun observeReminders(entryId: Long): Flow<List<EntryReminder>> = emptyFlow()

    override suspend fun setEntryRemindersEnabled(entryId: Long, isEnabled: Boolean) {
        enabledChanges += entryId to isEnabled
    }
}
