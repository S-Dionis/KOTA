package com.mwk.calendar.day

import java.time.LocalDate

data class DayTaskUiState(
    val id: Long,
    val title: String,
    val startTime: String,
    val endTime: String,
    val isTask: Boolean,
    val isEnabled: Boolean,
    val imagePath: String? = null,
)

data class DayScreenState(
    val today: LocalDate = LocalDate.now(),
    val showingDate: LocalDate = LocalDate.now(),
    val tasks: List<DayTaskUiState> = emptyList(),
)
