package com.mwk.calendar.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mwk.kota.DateUtils
import com.mwk.domain.api.ConsumeEntriesUseCase
import com.mwk.domain.model.Entry
import com.mwk.domain.repo.EntryReminderRepository
import com.mwk.domain.repo.EntryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class DayViewModel(
    private val consumeEntriesUseCase: ConsumeEntriesUseCase,
    private val entryRepository: EntryRepository,
    private val entryReminderRepository: EntryReminderRepository,
    private val zoneId: ZoneId,
    initialDate: LocalDate,
) : ViewModel() {

    private val _state = MutableStateFlow(DayScreenState(showingDate = initialDate))

    val state: StateFlow<DayScreenState> = _state.asStateFlow()

    private var currentDayEntries: List<Entry> = emptyList()

    init {
        requestEntries()
    }

    private fun requestEntries() {
        requestEntries(state.value.showingDate)
    }

    private fun requestEntries(localDate: LocalDate) {
        val (startDay, endDay) = getStartAndEndOfTheDay(localDate)
        consumeEntriesUseCase(startDay, endDay).onEach { entry ->
            currentDayEntries = entry
            updateUIState()
        }.launchIn(viewModelScope)
    }

    private fun updateUIState() {
        _state.update {
            it.copy(
                tasks = currentDayEntries.map { entry -> create(entry) }
            )
        }
    }


    private fun getStartAndEndOfTheDay(localDate: LocalDate): Pair<Long, Long> {
        val startDay = DateUtils.getStartOfDayMillis(localDate, zoneId)
        val endDay = DateUtils.getEndOfDayMillis(localDate, zoneId) + 1
        return Pair(startDay, endDay)
    }

    fun create(entry: Entry): DayTaskUiState {
        return DayTaskUiState(
            id = entry.id,
            title = entry.name,
            startTime = DateUtils.formatTimeMillis(entry.startTimeMillis, zoneId),
            endTime = DateUtils.formatTimeMillis(entry.endTimeMillis, zoneId),
            isTask = entry.isTask,
            isEnabled = entry.isEnabled,
            imagePath = entry.imagePath,
        )
    }

    fun onTaskCheckedChange(taskId: Long, isChecked: Boolean) {
        viewModelScope.launch {
            val isEnabled = !isChecked
            entryRepository.setEntryEnabled(taskId, isEnabled)
            entryReminderRepository.setEntryRemindersEnabled(taskId, isEnabled)
        }
    }

}
