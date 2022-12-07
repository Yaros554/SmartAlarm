package com.skyyaros.smartalarm.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.skyyaros.smartalarm.App
import com.skyyaros.smartalarm.background.AlarmReceiver
import com.skyyaros.smartalarm.background.CalcSunsetWorker
import com.skyyaros.smartalarm.datasource.AlarmElemRepo
import com.skyyaros.smartalarm.entity.AlarmElem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val alarmElemRepo: AlarmElemRepo = AlarmElemRepo.get()): ViewModel() {
    var canEdit = true
    var tempId = -1
    val alarms = alarmElemRepo.getAlarms().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    fun addOrUpdateAlarm(id: Int, hour: Int, minute: Int, context: Context, appContext: Context): Boolean {
        return if (canEdit) {
            if (id != -1) { //если это изменение времени будильника, то необходимо удалить старый сигнал
                deleteAlarmInManager(id, appContext)
            }
            viewModelScope.launch {
                var curId = id
                if (id == -1) //если добавление будильника, то получаем его id, иначе id у нас уже есть
                    curId = alarmElemRepo.insert(hour, minute)
                else
                    alarmElemRepo.updateAll(AlarmElem(id, hour, minute, null, null))
                val calcSunsetWorker = CalcSunsetWorker.createWorkRequest(curId, hour, minute)
                WorkManager.getInstance(context).beginUniqueWork(
                    "WorkName$curId",
                    ExistingWorkPolicy.REPLACE,
                    calcSunsetWorker
                ).enqueue()
            }
            true
        } else {
            false
        }
    }

    fun deleteAlarm(id: Int, context: Context): Boolean {
        return if (canEdit) {
            deleteAlarmInManager(id, context)
            viewModelScope.launch {
                alarmElemRepo.deleteAlarm(id)
            }
            true
        } else {
            false
        }
    }

    private fun deleteAlarmInManager(id: Int, context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = AlarmReceiver.createIntent(context)
        val pendingIntent = PendingIntent.getBroadcast(context, id, intent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)
    }
}