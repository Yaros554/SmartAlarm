package com.skyyaros.smartalarm.ui

import androidx.recyclerview.widget.RecyclerView
import com.skyyaros.smartalarm.databinding.AlarmItemBinding
import com.skyyaros.smartalarm.entity.AlarmElem

class AlarmViewHolder(val binding: AlarmItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: AlarmElem) {
        with(binding) {
            val tempHour = if (item.hour < 10) "0${item.hour}" else item.hour
            val tempMinute = if (item.minute < 10) "0${item.minute}" else item.minute
            time.text = "$tempHour:$tempMinute"
            if (item.sunset == null)
                sunset.text = "Восход солнца: рассчитывается"
            else {
                val tempDate = item.sunset!!.split("-")
                val formattedTempDate = tempDate.map {
                    if (it.toInt() < 10)
                        "0$it"
                    else
                        it
                }
                sunset.text = "${formattedTempDate[2]}-${formattedTempDate[1]}-${formattedTempDate[0]} ${formattedTempDate[3]}:${formattedTempDate[4]}"
            }
            switchAlarm.isChecked = true
            alarmId.text = item.id.toString()
        }
    }
}