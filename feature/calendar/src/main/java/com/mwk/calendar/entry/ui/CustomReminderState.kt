package com.mwk.calendar.entry.ui

import com.mwk.calendar.entry.model.RepeatFrequency
import com.mwk.kota.DateUtils
import java.time.DayOfWeek

data class RepeatState(
    val frequency: RepeatFrequency = RepeatFrequency.NEVER,
    val interval: Int = 1,
    val endType: RepeatEndType = RepeatEndType.NEVER,
    val daysOfWeek: Set<DayOfWeek> = emptySet(),
    val dayOfMonth: Int? = null,
    val monthOfYear: Int? = null,
    val maxOccurrences: Int? = null,
    val endDateMillis: Long? = null,
) {
    fun endDateToDisplayDate(): String {
        if (endDateMillis == null) return ""

        return DateUtils.formatDateMillis(endDateMillis)
    }
}

enum class RepeatEndType {
    NEVER,
    UNTIL_DATE,
    AFTER_COUNT
}
