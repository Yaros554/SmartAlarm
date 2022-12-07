package com.skyyaros.smartalarm.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.skyyaros.smartalarm.databinding.AlarmItemBinding
import com.skyyaros.smartalarm.entity.AlarmElem

class AlarmListAdapter(private val onClick: (AlarmElem) -> Unit, private val onSwitch: (Int) -> Unit): ListAdapter<AlarmElem, AlarmViewHolder>(DiffUtilCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        return AlarmViewHolder(AlarmItemBinding.inflate(LayoutInflater.from(parent.context), parent,false))
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.binding.root.setOnClickListener {
            onClick(item)
        }
        holder.binding.switchAlarm.setOnCheckedChangeListener { _, _ ->
            holder.binding.switchAlarm.isChecked = true
            onSwitch(item.id)
        }
    }
}

class DiffUtilCallback : DiffUtil.ItemCallback<AlarmElem>() {
    override fun areItemsTheSame(oldItem: AlarmElem, newItem: AlarmElem): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: AlarmElem, newItem: AlarmElem): Boolean = oldItem == newItem
}