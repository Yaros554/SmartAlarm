package com.skyyaros.smartalarm

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import com.skyyaros.smartalarm.datasource.AlarmElemRepo
import com.skyyaros.smartalarm.datasource.AlarmSound

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        AlarmElemRepo.initialize(this)
        AlarmSound.initialize(assets, getSharedPreferences("my_settings_sound", Context.MODE_PRIVATE))
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val name = "Smart alarm notification channel"
        val descriptionText = "Smart alarm notification channel for all notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "alarm_channel_id"
    }
}