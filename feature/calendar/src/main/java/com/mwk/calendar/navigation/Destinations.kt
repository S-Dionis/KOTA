package com.mwk.calendar.navigation

import kotlinx.serialization.Serializable

@Serializable
data class Calendar(val year: Int?, val month: Int?)

@Serializable
data class DayScreen(
    val year: Int,
    val month: Int,
    val day: Int,
)

@Serializable
data class EntryScreenDestination(
    val entryId: Long? = null,
)
