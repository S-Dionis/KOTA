package com.mwk.calendar.main

import java.time.LocalDate
import java.time.YearMonth

data class MainCalendarState(
    val today: LocalDate = LocalDate.now(),
    val startFromMonth: YearMonth = YearMonth.now(),
    val currentOnScreenMonth: YearMonth = YearMonth.now(),
    val entriesByDate: Map<LocalDate, List<MainDayEntry>> = emptyMap(),
)

data class MainDayEntry(
    val title: String,
    val isTask: Boolean,
    val isEnabled: Boolean,
)
