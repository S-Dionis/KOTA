package com.mwk.data.mapper

import com.mwk.data.entity.EntryEntity
import com.mwk.domain.model.Entry

fun EntryEntity.toDomain(): Entry {
    return Entry(
        id = id,
        name = name,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
        isTask = isTask,
        isEnabled = isEnabled,
        description = description,
        imagePath = imagePath,
    )
}

fun Entry.toEntity(): EntryEntity {
    return EntryEntity(
        id = id,
        name = name,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
        isTask = isTask,
        isEnabled = isEnabled,
        description = description,
        imagePath = imagePath,
    )
}
