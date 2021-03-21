package com.eventlocator.eventlocatororganizers.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.eventlocator.eventlocatororganizers.data.Event
import com.eventlocator.eventlocatororganizers.databinding.UpcomingEventBinding
import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormat
import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormatterFactory
import java.time.LocalDate

class UpcomingEventAdapter(private val upcomingEvents: ArrayList<Event>, private val status: ArrayList<String>):
        RecyclerView.Adapter<UpcomingEventAdapter.UpcomingEventHolder>() {

    inner class UpcomingEventHolder(var binding: UpcomingEventBinding): RecyclerView.ViewHolder(binding.root){
        init{
            binding.root.setOnClickListener {
                //TODO: open event page
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpcomingEventHolder {
        val binding = UpcomingEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UpcomingEventHolder(binding)


    }

    override fun onBindViewHolder(holder: UpcomingEventHolder, position: Int) {
        holder.binding.tvEventName.text = upcomingEvents[position].name
        val startDate = LocalDate.parse(upcomingEvents[position].startDate,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
        val endDate = LocalDate.parse(upcomingEvents[position].endDate,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
        holder.binding.tvEventDates.text = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY)
                .format(startDate) + " - " +
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY).format(endDate)

        holder.binding.tvEventStatus.text = status[position]

    }

    override fun getItemCount(): Int  = upcomingEvents.size
}