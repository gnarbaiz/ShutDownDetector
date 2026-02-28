package com.shutdowndetector.di

import android.content.Context
import androidx.room.Room
import com.shutdowndetector.data.local.dao.PowerEventDao
import com.shutdowndetector.data.local.database.PowerEventDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): PowerEventDatabase {
        return Room.databaseBuilder(
            context,
            PowerEventDatabase::class.java,
            "power_events_database"
        ).build()
    }

    @Provides
    fun providePowerEventDao(database: PowerEventDatabase): PowerEventDao {
        return database.powerEventDao()
    }
}
