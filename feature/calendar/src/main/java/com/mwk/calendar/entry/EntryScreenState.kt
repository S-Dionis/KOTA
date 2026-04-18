package com.mwk.calendar.entry

import com.mwk.calendar.entry.model.FilterType
import com.mwk.domain.model.EntryReminder

data class EntryScreenState(
    val event: EventState = EventState(),
    val notify: NotifyState = NotifyState()
) {
    val canSave: Boolean
        get() = if (event.filterType == FilterType.Event) {
            event.startTimeMillis != null &&
                    event.endTimeMillis != null &&
                    event.eventName.isNotBlank()
        } else {
            event.startTimeMillis != null &&
                    event.eventName.isNotBlank()
        }

    fun toDomainReminders(
        entryId: Long,
        startTimeMillis: Long,
    ): List<EntryReminder> {
        val notifyTimeMillis = NotifyTimeUtils.getNotifyTimeMillis(
            type = notify.type,
            startTimeMillis = startTimeMillis,
            customDateTimeMillis = notify.customDateTimeMillis,
        ) ?: return emptyList()
        val remindBeforeMillis = (startTimeMillis - notifyTimeMillis).coerceAtLeast(0L)

        return listOf(
            EntryReminder(
                entryId = entryId,
                remindBeforeMillis = remindBeforeMillis,
                isEnabled = notify.isEnabled,
            )
        )
    }
}

data class EventState(
    val id: Long? = null,
    val startTimeMillis: Long? = null,
    val endTimeMillis: Long? = null,
    val eventName: String = "",
    val description: String = "",
    val filterType: FilterType = FilterType.Event,
    val image: String? = null,
    val isEnabled: Boolean = true,
)

data class NotifyState(
    val type: NotifyType = NotifyType.NONE,
    val notifyTimes: List<Long> = emptyList(),
    val customDateTimeMillis: Long? = null,
    val isEnabled: Boolean = true,
)

enum class NotifyType {
    NONE,
    ONE_HOUR,
    ONE_DAY,
    CUSTOM
}
