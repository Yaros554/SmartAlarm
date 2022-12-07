package com.skyyaros.smartalarm.background

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.*
import com.skyyaros.smartalarm.App
import com.skyyaros.smartalarm.datasource.AlarmElemRepo
import com.skyyaros.smartalarm.ui.MainActivity
import com.skyyaros.smartalarm.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.shredzone.commons.suncalc.SunTimes
import java.util.*
import kotlin.random.Random

class CalcSunsetWorker(appContext: Context, workerParams: WorkerParameters): CoroutineWorker(appContext, workerParams) {
    companion object {
        const val WORK_TAG = "SunsetWorkRequest"
        private const val INPUT_KEY_ID = "${WORK_TAG}KeyId"
        private const val INPUT_KEY_HOUR = "${WORK_TAG}KeyHour"
        private const val INPUT_KEY_MINUTE = "${WORK_TAG}KeyMinute"

        fun createWorkRequest(id: Int, hour: Int, minute: Int): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<CalcSunsetWorker>()
                .setInputData(workDataOf(INPUT_KEY_ID to id, INPUT_KEY_HOUR to hour, INPUT_KEY_MINUTE to minute))
                .addTag(WORK_TAG)
                .build()
        }
    }

    override suspend fun doWork(): Result = coroutineScope {
        val id = inputData.getInt(INPUT_KEY_ID, -1)
        val hourUser = inputData.getInt(INPUT_KEY_HOUR, -1)
        val minuteUser = inputData.getInt(INPUT_KEY_MINUTE, -1)

        val lastLocationProvider = LastLocationProvider(applicationContext)
        val locationResult = lastLocationProvider.getLocation()
        if (locationResult.isSuccess) { //если успешно местоположение определили
            val location = locationResult.getOrNull()!!
            val calendar = Calendar.getInstance()
            val curEpochTime = calendar.timeInMillis / 1000
            var curDay = calendar.get(Calendar.DAY_OF_MONTH)
            var curMonth = calendar.get(Calendar.MONTH) + 1
            var curYear = calendar.get(Calendar.YEAR)
            var sunInfo = SunTimes.compute()
                .on(curYear, curMonth, curDay)
                .latitude(location.latitude)
                .longitude(location.longitude)
                .execute()
            var yearRise = sunInfo.rise!!.year
            var monthRise = sunInfo.rise!!.month.value
            var dayRise = sunInfo.rise!!.dayOfMonth
            var hourRise = sunInfo.rise!!.hour
            var minuteRise = sunInfo.rise!!.minute
            val epochTimeRise = sunInfo.rise!!.toEpochSecond()

            //если время будильника уже прошло, то это значит, что надо ставить будильник на СЛЕДУЮЩИЙ рассвет
            val needRepeat = epochTimeRise + hourUser*60*60 + minuteUser*60 <= curEpochTime
            if (needRepeat) {
                calendar.add(Calendar.DATE, 1)
                curDay = calendar.get(Calendar.DAY_OF_MONTH)
                curMonth = calendar.get(Calendar.MONTH) + 1
                curYear = calendar.get(Calendar.YEAR)
                sunInfo = SunTimes.compute()
                    .on(curYear, curMonth, curDay)
                    .latitude(location.latitude)
                    .longitude(location.longitude)
                    .execute()
                yearRise = sunInfo.rise!!.year
                monthRise = sunInfo.rise!!.month.value
                dayRise = sunInfo.rise!!.dayOfMonth
                hourRise = sunInfo.rise!!.hour
                minuteRise = sunInfo.rise!!.minute
            }

            val timeInMillisForAlarm = (epochTimeRise + hourUser*60*60 + minuteUser*60) * 1000
            AlarmElemRepo.initialize(applicationContext)
            val alarmElemRepo = AlarmElemRepo.get()
            //обновляем инфу в бд, дописывая туда то, что только что рассчитали
            alarmElemRepo.updateSunset("$yearRise-$monthRise-$dayRise-$hourRise-$minuteRise", timeInMillisForAlarm, id)
            createNotification("Время восхода успешно определено", "Время восхода солнца: $yearRise-$monthRise-$dayRise $hourRise:$minuteRise")
            createAlarm(timeInMillisForAlarm, id)
            return@coroutineScope Result.success()
        } else {
            val message = locationResult.exceptionOrNull()!!.message!!
            if (message == "No location found") //если не получилось определить местоположение, то попробуем снова
                return@coroutineScope Result.retry()
            else {
                AlarmElemRepo.initialize(applicationContext) //если нет разрешения, то будильник не ставим
                val alarmElemRepo = AlarmElemRepo.get()
                alarmElemRepo.deleteAlarm(id)
                createNotification("Ошибка!", "Нет разрешения на определение местоположения. Будильник не установлен")
                return@coroutineScope Result.failure()
            }
        }
    }

    private fun createNotification(title: String, text: String) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_MUTABLE)
        else
            PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(applicationContext, App.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.add_alarm)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(Random.nextInt(), notification)
    }

    private fun createAlarm(alarmTimeInMillis: Long, id: Int) {
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmType = AlarmManager.RTC_WAKEUP
        val intent = AlarmReceiver.createIntent(applicationContext)
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, id, intent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setExactAndAllowWhileIdle(
            alarmType,
            alarmTimeInMillis,
            pendingIntent
        )
    }
}