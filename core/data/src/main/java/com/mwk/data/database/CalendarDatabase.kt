package com.mwk.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mwk.data.dao.DayDao
import com.mwk.data.dao.EntryNotificationDao
import com.mwk.data.dao.EntryDao
import com.mwk.data.entity.EntryNotificationEntity
import com.mwk.data.entity.EntryEntity

@Database(
    entities = [EntryEntity::class, EntryNotificationEntity::class],
    version = 5,
    exportSchema = false
)
abstract class CalendarDatabase : RoomDatabase() {

    abstract fun entryDao(): EntryDao
    abstract fun entryNotificationDao(): EntryNotificationDao

    abstract fun dayDao(): DayDao

}
