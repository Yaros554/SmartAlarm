package com.skyyaros.smartalarm.ui

interface AlarmSetCallback {
    fun onAlarmSet(itemId: Int, hourOfDay: Int, minute: Int)
    fun deleteAlarm(id: Int)
}