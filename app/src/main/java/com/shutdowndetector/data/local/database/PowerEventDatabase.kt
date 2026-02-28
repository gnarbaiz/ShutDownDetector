package com.shutdowndetector.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shutdowndetector.data.local.dao.PowerEventDao
import com.shutdowndetector.data.local.entity.PowerEventEntity

@Database(
    entities = [PowerEventEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class PowerEventDatabase : RoomDatabase() {
    abstract fun powerEventDao(): PowerEventDao
}
