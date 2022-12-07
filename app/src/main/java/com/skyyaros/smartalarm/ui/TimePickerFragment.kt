package com.skyyaros.smartalarm.ui

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class TimePickerFragment(private val itemId: Int) : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    private var alarmSetCallback: AlarmSetCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        alarmSetCallback = context as AlarmSetCallback
        retainInstance = true
    }

    override fun onDetach() {
        alarmSetCallback = null
        super.onDetach()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return TimePickerDialog(activity, this, hour, minute, true)
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        alarmSetCallback!!.onAlarmSet(itemId, hourOfDay, minute)
    }
}