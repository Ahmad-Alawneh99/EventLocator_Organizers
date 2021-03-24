package com.eventlocator.eventlocatororganizers.adapters



import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.eventlocator.eventlocatororganizers.data.Event
import com.eventlocator.eventlocatororganizers.databinding.PreviousEventBinding
import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormat
import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormatterFactory
import java.time.LocalDate

class PreviousEventAdapter(private val previousEvents: ArrayList<Event>):
        RecyclerView.Adapter<PreviousEventAdapter.PreviousEventHolder>() {

    inner class PreviousEventHolder(var binding: PreviousEventBinding): RecyclerView.ViewHolder(binding.root){
        init{
            binding.root.setOnClickListener {
                //TODO: open event page
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviousEventHolder {
        val binding = PreviousEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PreviousEventHolder(binding)


    }

    override fun onBindViewHolder(holder: PreviousEventHolder, position: Int) {
        holder.binding.tvEventName.text = previousEvents[position].name
        val startDate = LocalDate.parse(previousEvents[position].startDate,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
        val endDate = LocalDate.parse(previousEvents[position].endDate,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
        holder.binding.tvEventDates.text = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY)
                .format(startDate) + " - " +
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY).format(endDate)

        holder.binding.tvEventRating.text = previousEvents[position].rating.toString() + "/5"

    }

    override fun getItemCount(): Int  = previousEvents.size
}