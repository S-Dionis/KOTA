package com.mwk.data.di

import android.content.Context
import androidx.room.Room
import com.mwk.data.dao.DayDao
import com.mwk.data.dao.EntryNotificationDao
import com.mwk.data.dao.EntryDao
import com.mwk.data.database.CalendarDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCalendarDatabase(@ApplicationContext context: Context): CalendarDatabase {
        return Room.databaseBuilder(
            context,
            CalendarDatabase::class.java,
            "calendar.db"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }


    @Provides
    fun provideEntryDao(calendarDatabase: CalendarDatabase): EntryDao {
        return calendarDatabase.entryDao()
    }

    @Provides
    fun provideEntryNotificationDao(calendarDatabase: CalendarDatabase): EntryNotificationDao {
        return calendarDatabase.entryNotificationDao()
    }

    @Provides
    fun provideDayDao(calendarDatabase: CalendarDatabase): DayDao {
        return calendarDatabase.dayDao()
    }

}
