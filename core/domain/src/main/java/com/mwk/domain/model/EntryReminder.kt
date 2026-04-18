package com.mwk.domain.model

data class EntryReminder(
    val id: Long = 0L,
    val entryId: Long,
    val remindBeforeMillis: Long,
    val isEnabled: Boolean = true,
)
