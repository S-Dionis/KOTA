package com.mwk.calendar.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mwk.calendar.entry.model.FilterType
import com.mwk.domain.api.SaveEntryUseCase
import com.mwk.domain.model.Entry
import com.mwk.domain.model.EntryReminder
import com.mwk.domain.repo.EntryReminderRepository
import com.mwk.domain.repo.EntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class EntryViewModel @Inject constructor(
    private val saveEntryUseCase: SaveEntryUseCase,
    private val entryRepository: EntryRepository,
    private val entryReminderRepository: EntryReminderRepository,
    private val zoneId: ZoneId,
) : ViewModel() {

    private val _state = MutableStateFlow(EntryScreenState())
    private var observeEntryJob: Job? = null

    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<EntryUiEvent>()
    val events = _events.asSharedFlow()

    fun loadEntry(entryId: Long?) {
        if (entryId == null) return
        if (_state.value.event.id == entryId) return

        observeEntryJob?.cancel()
        observeEntryJob = viewModelScope.launch {
            combine(
                entryRepository.observeEntry(entryId),
                entryReminderRepository.observeReminders(entryId),
            ) { entry, reminders ->
                entry to reminders
            }.collect { (entry, reminders) ->
                entry ?: return@collect

                _state.value = EntryScreenState(
                    event = EventState(
                        id = entry.id,
                        startTimeMillis = entry.startTimeMillis,
                        endTimeMillis = if (entry.isTask) null else entry.endTimeMillis,
                        eventName = entry.name,
                        description = entry.description,
                        filterType = if (entry.isTask) FilterType.Task else FilterType.Event,
                        image = entry.imagePath,
                        isEnabled = entry.isEnabled,
                    ),
                    notify = reminders.firstOrNull()?.toNotifyState(entry.startTimeMillis) ?: NotifyState(),
                )
            }
        }
    }

    fun onNameChange(name: String) {
        _state.update { state ->
            state.copy(
                event = state.event.copy(eventName = name)
            )
        }
    }

    fun onFilterChange(filterType: FilterType) {
        _state.update { state ->
            state.copy(
                event = state.event.copy(filterType = filterType)
            )
        }
    }

    fun onStartDateChange(date: Long?) {
        _state.update { state ->
            state.copy(
                event = state.event.copy(startTimeMillis = date)
            )
        }
    }

    fun onEndDateChange(date: Long?) {
        _state.update { state ->
            state.copy(
                event = state.event.copy(endTimeMillis = date)
            )
        }
    }

    fun onNotifyOptionSelected(notifyState: NotifyState) {
        _state.update { state ->
            state.copy(
                notify = notifyState
            )
        }
    }

    fun onDescriptionChanged(description: String) {
        _state.update { state ->
            state.copy(
                event = state.event.copy(description = description)
            )
        }
    }

    fun onImageChanged(imagePath: String?) {
        _state.update { state ->
            state.copy(
                event = state.event.copy(image = imagePath)
            )
        }
    }

    fun onSaveClick() {
        val stateValue = state.value

        if (stateValue.event.filterType == FilterType.Event &&
            (stateValue.event.startTimeMillis == null ||
                stateValue.event.eventName.isBlank() ||
                stateValue.event.endTimeMillis == null)
        ) {
            return
        } else if (stateValue.event.startTimeMillis == null || stateValue.event.eventName.isBlank()) {
            return
        }

        viewModelScope.launch {
            val entry = buildEntry(stateValue) ?: return@launch
            val reminders = stateValue.toDomainReminders(
                entryId = entry.id,
                startTimeMillis = entry.startTimeMillis
            )

            saveEntryUseCase(
                entry = entry,
                reminders = reminders
            )

            _events.emit(EntryUiEvent.SaveSuccess)
        }
    }

    private fun buildEntry(state: EntryScreenState): Entry? {
        val startTimeMillis = state.event.startTimeMillis ?: return null
        val endTimeMillis = when {
            state.event.endTimeMillis != null -> state.event.endTimeMillis
            state.event.filterType == FilterType.Task -> getNextDayStartMillis(startTimeMillis)
            else -> startTimeMillis
        }

        return Entry(
            id = state.event.id ?: 0L,
            name = state.event.eventName,
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
            isTask = state.event.filterType == FilterType.Task,
            isEnabled = state.event.isEnabled,
            description = state.event.description,
            imagePath = state.event.image,
        )
    }

    private fun getNextDayStartMillis(timeMillis: Long): Long {
        val localDate = Instant.ofEpochMilli(timeMillis)
            .atZone(zoneId)
            .toLocalDate()
            .plusDays(1)

        return localDate
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    private fun EntryReminder.toNotifyState(startTimeMillis: Long): NotifyState {
        val customDateTimeMillis = (startTimeMillis - remindBeforeMillis).coerceAtLeast(0L)
        val type = NotifyTimeUtils.getNotifyType(remindBeforeMillis)

        return NotifyState(
            type = type,
            notifyTimes = listOf(customDateTimeMillis),
            customDateTimeMillis = if (type == NotifyType.CUSTOM) customDateTimeMillis else null,
            isEnabled = isEnabled,
        )
    }
}
