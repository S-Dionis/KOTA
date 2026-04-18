package com.mwk.calendar.entry

import com.mwk.domain.api_impl.SaveEntryUseCaseImpl
import com.mwk.calendar.entry.model.FilterType
import com.mwk.domain.model.Entry
import com.mwk.domain.model.EntryReminder
import com.mwk.domain.repo.EntryReminderRepository
import com.mwk.domain.repo.EntryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class EntryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `onNameChange update`() {
        val viewModel = createViewModel()

        viewModel.onNameChange("Оч важно")

        assertEquals("Оч важно", viewModel.state.value.event.eventName)
    }

    @Test
    fun `onSaveClick do not save enrty when required fields missing`() = runTest {
        val entryRepository = FakeEntryRepository()
        val reminderRepository = FakeEntryReminderRepository()
        val viewModel = createViewModel(entryRepository, reminderRepository)

        viewModel.onFilterChange(FilterType.Event)
        viewModel.onNameChange("Встреча")
        viewModel.onStartDateChange(1_000L)

        viewModel.onSaveClick()
        advanceUntilIdle()

        assertEquals(0, entryRepository.savedEntries.size)
        assertEquals(0, reminderRepository.replaceCalls.size)
    }

    @Test
    fun `onSaveClick save task and emits entry when state valid`() = runTest {
        val entryRepository = FakeEntryRepository()
        val reminderRepository = FakeEntryReminderRepository()
        val viewModel = createViewModel(entryRepository, reminderRepository)

        viewModel.onFilterChange(FilterType.Task)
        viewModel.onNameChange("Купить")
        viewModel.onDescriptionChanged("Денег")
        viewModel.onStartDateChange(5_000L)

        val successEvent = async { viewModel.events.first() }

        viewModel.onSaveClick()
        advanceUntilIdle()

        assertEquals(1, entryRepository.savedEntries.size)

        val savedEntry = entryRepository.savedEntries.single().entry
        val expectedEndTime = Instant.ofEpochMilli(5_000L)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        assertEquals("Купить", savedEntry.name)
        assertEquals(5_000L, savedEntry.startTimeMillis)
        assertEquals(expectedEndTime, savedEntry.endTimeMillis)
        assertTrue(savedEntry.isTask)
        assertEquals("Денег", savedEntry.description)

        assertEquals(1, reminderRepository.replaceCalls.size)
        assertEquals(42L, reminderRepository.replaceCalls.single().entryId)
        assertTrue(reminderRepository.replaceCalls.single().reminders.isEmpty())
        assertEquals(EntryUiEvent.SaveSuccess, successEvent.await())
    }

    private fun createViewModel(
        entryRepository: FakeEntryRepository = FakeEntryRepository(),
        reminderRepository: FakeEntryReminderRepository = FakeEntryReminderRepository(),
    ): EntryViewModel {
        val saveEntryUseCase = SaveEntryUseCaseImpl(
            entryRepository = entryRepository,
            entryReminderRepository = reminderRepository
        )

        return EntryViewModel(
            saveEntryUseCase = saveEntryUseCase,
            entryRepository = entryRepository,
            entryReminderRepository = reminderRepository,
            zoneId = ZoneId.systemDefault(),
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

private class FakeEntryRepository : EntryRepository {
    val savedEntries = mutableListOf<SavedEntryCall>()
    var nextId: Long = 42L

    override suspend fun saveEntry(entry: Entry): Long {
        savedEntries += SavedEntryCall(entry)
        return nextId
    }

    override fun observeEntries(startTimeMillis: Long, endTimeMillis: Long) = emptyFlow<List<Entry>>()

    override fun observeEntry(id: Long) = emptyFlow<Entry?>()

    override suspend fun setEntryEnabled(entryId: Long, isEnabled: Boolean) = Unit
}

private data class SavedEntryCall(
    val entry: Entry,
)

private class FakeEntryReminderRepository : EntryReminderRepository {
    val replaceCalls = mutableListOf<ReplaceRemindersCall>()

    override suspend fun replaceReminders(entryId: Long, reminders: List<EntryReminder>) {
        replaceCalls += ReplaceRemindersCall(entryId, reminders)
    }

    override fun observeReminders(entryId: Long) = emptyFlow<List<EntryReminder>>()

    override suspend fun setEntryRemindersEnabled(entryId: Long, isEnabled: Boolean) = Unit
}

private data class ReplaceRemindersCall(
    val entryId: Long,
    val reminders: List<EntryReminder>,
)
