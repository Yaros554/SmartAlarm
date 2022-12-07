package com.skyyaros.smartalarm.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.skyyaros.smartalarm.R
import com.skyyaros.smartalarm.databinding.ActivityAlarmBinding
import com.skyyaros.smartalarm.datasource.AlarmSound

class AlarmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlarmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lottieView.setAnimation(R.raw.alarm)
        AlarmSound.initialize(assets, applicationContext.getSharedPreferences("my_settings_sound", Context.MODE_PRIVATE))
        val alarmSound = AlarmSound.get()
        alarmSound.play()

        binding.button.setOnClickListener {
            alarmSound.stop()
            finish()
        }
    }
}