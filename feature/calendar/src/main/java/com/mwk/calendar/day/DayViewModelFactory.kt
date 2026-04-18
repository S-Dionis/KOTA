package com.mwk.calendar.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mwk.domain.api.ConsumeEntriesUseCase
import com.mwk.domain.repo.EntryReminderRepository
import com.mwk.domain.repo.EntryRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class DayViewModelFactory @Inject constructor(
    private val consumeEntriesUseCase: ConsumeEntriesUseCase,
    private val entryRepository: EntryRepository,
    private val entryReminderRepository: EntryReminderRepository,
    private val zoneId: ZoneId,
) {

    fun create(initialDate: LocalDate): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                DayViewModel(
                    consumeEntriesUseCase = consumeEntriesUseCase,
                    entryRepository = entryRepository,
                    entryReminderRepository = entryReminderRepository,
                    zoneId = zoneId,
                    initialDate = initialDate,
                )
            }
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DayViewModelFactoryEntryPoint {
    fun dayViewModelFactory(): DayViewModelFactory
}
