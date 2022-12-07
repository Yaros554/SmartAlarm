package com.skyyaros.smartalarm.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.skyyaros.smartalarm.R
import com.skyyaros.smartalarm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), AlarmSetCallback {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private val alarmListAdapter = AlarmListAdapter (
        { alarm ->
            viewModel.tempId = alarm.id
            requestPermissions()
        },
        { id -> DeleteDialogFragment(id).show(supportFragmentManager, "deleteAlarm") }
    )
    private val launcherLocation = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
        if (map.values.isNotEmpty() && (map.values.toList()[0] || map.values.toList()[1])) {
            launcherBackgroundLocation.launch(REQUIRED_PERMISSION[2])
        } else {
            Toast.makeText(this, "Необходимо разрешение на определение местоположения!", Toast.LENGTH_LONG).show()
        }
    }
    private val launcherBackgroundLocation = registerForActivityResult(ActivityResultContracts.RequestPermission()) { permission ->
        if (permission) {
            TimePickerFragment(viewModel.tempId).show(supportFragmentManager, "timePicker")
        } else {
            Toast.makeText(this, "Необходимо разрешение на местоположение в фоне!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recycler.adapter = alarmListAdapter
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launchWhenStarted {
            viewModel.alarms.collect {
                alarmListAdapter.submitList(it)
                if (it.isEmpty()) {
                    binding.imageView.visibility = View.VISIBLE
                    binding.textView.visibility = View.VISIBLE
                    viewModel.canEdit = true
                } else {
                    binding.imageView.visibility = View.GONE
                    binding.textView.visibility = View.GONE
                    val el = it.find { elem -> elem.sunset == null }
                    viewModel.canEdit = el == null
                }
            }
        }
        binding.imageButton.setOnClickListener {
            viewModel.tempId = -1
            requestPermissions()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.music -> {
                SoundDialogFragment().show(supportFragmentManager, "soundDialogFragment")
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onAlarmSet(itemId: Int, hourOfDay: Int, minute: Int) {
        val res = viewModel.addOrUpdateAlarm(itemId, hourOfDay, minute, this, applicationContext)
        if (!res)
            Toast.makeText(this, "Пожалуйста, дождитесь завершения расчетов и повторите попытку", Toast.LENGTH_LONG).show()
    }

    override fun deleteAlarm(id: Int) {
        val res = viewModel.deleteAlarm(id, applicationContext)
        if (!res)
            Toast.makeText(this, "Пожалуйста, дождитесь завершения расчетов и повторите попытку", Toast.LENGTH_LONG).show()
    }

    private val REQUIRED_PERMISSION: Array<String> = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    private fun requestPermissions() {
        val allowedPermissions = REQUIRED_PERMISSION.map { permission ->
            checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        }
        if (allowedPermissions[2] && (allowedPermissions[0] || allowedPermissions[1])) {
            TimePickerFragment(viewModel.tempId).show(supportFragmentManager, "timePicker")
        } else if (allowedPermissions[0] || allowedPermissions[1]) {
            launcherBackgroundLocation.launch(REQUIRED_PERMISSION[2])
        } else {
            launcherLocation.launch(REQUIRED_PERMISSION.filter { it != Manifest.permission.ACCESS_BACKGROUND_LOCATION }.toTypedArray())
        }
    }
}