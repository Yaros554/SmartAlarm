package com.skyyaros.smartalarm.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.skyyaros.smartalarm.datasource.AlarmSound

class SoundDialogFragment: DialogFragment() {
    private var selectedId: Int? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        retainInstance = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Выберите звук")
            val alarmSound = AlarmSound.get()
            val data = alarmSound.getSounds()
            builder.setSingleChoiceItems(data, alarmSound.currentSoundIndex) { _, which ->
                selectedId = which
                alarmSound.play(which)
            }
            builder.setPositiveButton("Ok") { _, _ ->
                if (selectedId != null) {
                    alarmSound.currentSoundIndex = selectedId!!
                    alarmSound.saveSettings()
                }
                alarmSound.stop()
                dismiss()
            }
            builder.setNegativeButton("Отмена") { _, _ ->
                alarmSound.stop()
                dismiss()
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}