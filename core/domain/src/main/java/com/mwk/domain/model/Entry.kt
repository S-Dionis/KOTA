package com.mwk.domain.model

data class Entry(
    val id: Long,
    val name: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val isTask: Boolean = false,
    val isEnabled: Boolean = true,
    val description: String = "",
    val imagePath: String? = null,
)
