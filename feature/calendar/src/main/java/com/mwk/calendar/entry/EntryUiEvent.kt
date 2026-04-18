package com.mwk.calendar.entry

sealed interface EntryUiEvent {
    data object SaveSuccess : EntryUiEvent
}