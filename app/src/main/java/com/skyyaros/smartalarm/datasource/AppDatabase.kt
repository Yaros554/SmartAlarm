package com.skyyaros.smartalarm.datasource

import androidx.room.Database
import androidx.room.RoomDatabase
import com.skyyaros.smartalarm.entity.AlarmElem

@Database(entities = [AlarmElem::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
}