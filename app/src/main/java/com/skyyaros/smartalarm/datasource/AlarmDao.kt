package com.skyyaros.smartalarm.datasource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.skyyaros.smartalarm.entity.AlarmElem
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarmElems")
    fun getAlarms(): Flow<List<AlarmElem>>
    @Query("SELECT * FROM alarmElems")
    suspend fun getAlarmsBackground(): List<AlarmElem>
    @Insert
    suspend fun insert(alarmElem: AlarmElem)
    @Update
    suspend fun updateAll(alarmElem: AlarmElem)
    @Query("UPDATE alarmElems SET sunset = :sunset, alarmInMillis = :alarmInMillis WHERE id = :id")
    suspend fun updateSunset(sunset: String, alarmInMillis: Long,  id: Int)
    @Query("DELETE FROM alarmElems WHERE id = :id")
    suspend fun deleteAlarm(id: Int)
    @Query("SELECT id FROM alarmElems")
    fun getIds(): List<Int>
}