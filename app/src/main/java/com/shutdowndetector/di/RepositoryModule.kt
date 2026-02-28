package com.shutdowndetector.di

import com.shutdowndetector.data.remote.EmailSenderImpl
import com.shutdowndetector.data.repository.PowerEventRepositoryImpl
import com.shutdowndetector.domain.repository.EmailSender
import com.shutdowndetector.domain.repository.PowerEventRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPowerEventRepository(
        repositoryImpl: PowerEventRepositoryImpl
    ): PowerEventRepository

    @Binds
    @Singleton
    abstract fun bindEmailSender(
        emailSenderImpl: EmailSenderImpl
    ): EmailSender
}
