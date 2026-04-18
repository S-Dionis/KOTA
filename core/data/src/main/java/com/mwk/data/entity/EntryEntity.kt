package com.mwk.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val isTask: Boolean,
    val isEnabled: Boolean = true,
    val description: String = "",
    val imagePath: String? = null,
)
