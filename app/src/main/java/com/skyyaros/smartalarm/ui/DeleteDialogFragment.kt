package com.skyyaros.smartalarm.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class DeleteDialogFragment(private val alarmId: Int): DialogFragment() {
    private var alarmDeleteCallback: AlarmSetCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        alarmDeleteCallback = context as AlarmSetCallback
        retainInstance = true
    }

    override fun onDetach() {
        alarmDeleteCallback = null
        super.onDetach()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage("Удалить будильник?")
                .setPositiveButton("Да") { _, _ ->
                    alarmDeleteCallback!!.deleteAlarm(alarmId)
                    dismiss()
                }
                .setNegativeButton("Нет") { _, _ ->
                    dismiss()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}