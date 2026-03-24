package com.offlineledger.ui.logs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.offlineledger.data.model.ReminderLog
import com.offlineledger.databinding.ItemLogBinding
import java.text.SimpleDateFormat
import java.util.*

class LogAdapter : ListAdapter<ReminderLog, LogAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemLogBinding) :
        RecyclerView.ViewHolder(b.root) {

        private val dateTimeFmt = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())

        fun bind(log: ReminderLog) {
            b.tvLogName.text = log.personName
            b.tvLogMobile.text = log.mobileNumber
            b.tvLogMessage.text = log.message
            b.tvLogTimestamp.text = dateTimeFmt.format(Date(log.sentAt))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ReminderLog>() {
            override fun areItemsTheSame(a: ReminderLog, b: ReminderLog) = a.id == b.id
            override fun areContentsTheSame(a: ReminderLog, b: ReminderLog) = a == b
        }
    }
}
