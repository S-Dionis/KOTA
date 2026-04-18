package com.mwk.domain.di

import com.mwk.domain.api.ConsumeEntriesUseCase
import com.mwk.domain.api.ObserveMainEntriesUseCase
import com.mwk.domain.api.SaveEntryUseCase
import com.mwk.domain.api_impl.ConsumeEntriesUseCaseImpl
import com.mwk.domain.api_impl.ObserveMainEntriesUseCaseImpl
import com.mwk.domain.api_impl.SaveEntryUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DomainModule {

    @Binds
    @Singleton
    fun bindConsumeEntriesUseCase(
        useCase: ConsumeEntriesUseCaseImpl
    ): ConsumeEntriesUseCase

    @Binds
    @Singleton
    fun bindSaveEntryUseCase(
        useCase: SaveEntryUseCaseImpl
    ): SaveEntryUseCase

    @Binds
    @Singleton
    fun bindObserveMainEntriesUseCase(
        useCase: ObserveMainEntriesUseCaseImpl
    ): ObserveMainEntriesUseCase

}