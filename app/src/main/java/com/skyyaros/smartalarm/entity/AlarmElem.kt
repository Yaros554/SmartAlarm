package com.skyyaros.smartalarm.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarmElems")
data class AlarmElem(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int, //уникальный id
    @ColumnInfo(name = "hour")
    var hour: Int, //час, который выбрал пользователь
    @ColumnInfo(name = "minute")
    var minute: Int, //минута, которую выбрал пользователь
    @ColumnInfo(name = "sunset")
    var sunset: String?, //строка со временем рассвета в формате год-месяц-день-час-минута
    @ColumnInfo(name = "alarmInMillis")
    var alarmInMillis: Long? //время в миллисекундах, когда должен прозвенить будильник
)
