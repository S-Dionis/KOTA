package com.mwk.data.di

import com.mwk.data.repository.DayRepositoryImpl
import com.mwk.data.repository.EntryRepositoryImpl
import com.mwk.data.repository.EntryReminderRepositoryImpl
import com.mwk.domain.repo.DayRepository
import com.mwk.domain.repo.EntryReminderRepository
import com.mwk.domain.repo.EntryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindEntryRepository(
        entryRepository: EntryRepositoryImpl
    ): EntryRepository

    @Binds
    @Singleton
    fun bindEntryReminderRepository(
        entryReminderRepository: EntryReminderRepositoryImpl,
    ): EntryReminderRepository

    @Binds
    @Singleton
    fun bindDayRepository(
        dayRepository: DayRepositoryImpl
    ): DayRepository

}
