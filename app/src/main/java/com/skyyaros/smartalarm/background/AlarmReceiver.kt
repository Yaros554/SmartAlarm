package com.skyyaros.smartalarm.background

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.skyyaros.smartalarm.App
import com.skyyaros.smartalarm.R
import com.skyyaros.smartalarm.datasource.AlarmElemRepo
import com.skyyaros.smartalarm.datasource.AlarmSound
import com.skyyaros.smartalarm.ui.AlarmActivity
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val ALARM_ACTION = "W_ACTION"

        fun createIntent(context: Context): Intent {
            return Intent(context, AlarmReceiver::class.java).apply {
                action = ALARM_ACTION
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ALARM_ACTION) {
            return
        }
        AlarmElemRepo.initialize(context)
        val alarmElemRepo = AlarmElemRepo.get()
        val listAlarmElem = runBlocking { alarmElemRepo.getAlarmsBackGround() }
        //надо найти будильник с самым маленьким временем, нас система разбудила именно из-за него
        val tempAlarmElem = listAlarmElem.minBy { it.alarmInMillis!! }
        val alarmHour = tempAlarmElem.hour
        val alarmMinute = tempAlarmElem.minute
        val alarmTime = "${if (alarmHour < 10) "0$alarmHour" else alarmHour}:${if (alarmMinute < 10) "0$alarmMinute" else alarmMinute}"
        val alarmId = tempAlarmElem.id
        //createNotification(context, alarmTime)
        //AlarmSound.initialize(context.assets, context.getSharedPreferences("my_settings_sound", Context.MODE_PRIVATE))
        //val alarmSound = AlarmSound.get()
        //alarmSound.play()
        context.startActivity(Intent(context, AlarmActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        //необходимо удалить информацию из БД об отработавшем будильнике
        runBlocking { alarmElemRepo.deleteAlarm(alarmId) }
    }

    private fun createNotification(context: Context, alarmTime: String) {
        val intent = Intent(context, AlarmActivity::class.java)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE)
        else
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(context, App.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.add_alarm)
            .setContentTitle("Будильник $alarmTime")
            .setContentText("Нажмите на уведомление!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(Random.nextInt(), notification)
    }
}