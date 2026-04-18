package com.mwk.data.mapper

import com.mwk.data.entity.EntryNotificationEntity
import com.mwk.domain.model.EntryReminder

fun EntryReminder.toEntity(): EntryNotificationEntity {
    return EntryNotificationEntity(
        id = id,
        entryId = entryId,
        remindBeforeMillis = remindBeforeMillis,
        isEnabled = isEnabled,
    )
}

fun EntryNotificationEntity.toDomain(): EntryReminder {
    return EntryReminder(
        id = id,
        entryId = entryId,
        remindBeforeMillis = remindBeforeMillis,
        isEnabled = isEnabled,
    )
}
