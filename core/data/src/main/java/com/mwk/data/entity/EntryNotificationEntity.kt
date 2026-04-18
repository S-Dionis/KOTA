package com.mwk.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = EntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["entryId"]),
    ],
)
data class EntryNotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val entryId: Long,
    val remindBeforeMillis: Long,
    val isEnabled: Boolean = true,
    val lastTriggeredOccurrenceStartMillis: Long? = null,
)
