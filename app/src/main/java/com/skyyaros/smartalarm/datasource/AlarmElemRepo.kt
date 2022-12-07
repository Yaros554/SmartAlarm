package com.skyyaros.smartalarm.datasource

import android.content.Context
import androidx.room.Room
import com.skyyaros.smartalarm.entity.AlarmElem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

private const val DATABASE_NAME = "AlarmDb"
class AlarmElemRepo private constructor(context: Context) {
    private var maxId = -1
    private val db : AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        DATABASE_NAME)
        .build()
    init {
        Thread {
            val ids = db.alarmDao().getIds() //обеспечиваем уникальность id в таблице будильников
            maxId = if (ids.isNotEmpty())
                ids.max() + 1
            else
                0
        }.start()
    }

    fun getAlarms(): Flow<List<AlarmElem>> {
        return db.alarmDao().getAlarms()
    }

    suspend fun getAlarmsBackGround(): List<AlarmElem> {
        return db.alarmDao().getAlarmsBackground()
    }

    suspend fun insert(hour: Int, minute: Int): Int {
        while (maxId == -1)
            delay(100) //ждем пока не рассчитали нужный id
        val alarmElem = AlarmElem(maxId, hour, minute, null, null)
        maxId++
        db.alarmDao().insert(alarmElem)
        return alarmElem.id
    }

    suspend fun updateSunset(sunset: String, alarmInMillis: Long, id: Int) {
        db.alarmDao().updateSunset(sunset, alarmInMillis, id)
    }

    suspend fun updateAll(alarmElem: AlarmElem) {
        db.alarmDao().updateAll(alarmElem)
    }

    suspend fun deleteAlarm(id: Int) {
        db.alarmDao().deleteAlarm(id)
    }

    companion object {
        private var INSTANCE: AlarmElemRepo? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = AlarmElemRepo(context)
            }
        }
        fun get(): AlarmElemRepo {
            return INSTANCE ?: throw IllegalStateException("AlarmElemRepo must be initialized")
        }
    }
}