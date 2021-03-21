package com.eventlocator.eventlocatororganizers.adapters



import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.eventlocator.eventlocatororganizers.data.Event
import com.eventlocator.eventlocatororganizers.databinding.CanceledEventBinding
import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormat
import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormatterFactory
import java.time.LocalDate

class CanceledEventAdapter(private val cacneledEvents: ArrayList<Event>):
        RecyclerView.Adapter<CanceledEventAdapter.CanceledEventHolder>() {

    inner class CanceledEventHolder(var binding: CanceledEventBinding): RecyclerView.ViewHolder(binding.root){
        init{
            binding.root.setOnClickListener {
                //TODO: open event page
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CanceledEventHolder {
        val binding = CanceledEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CanceledEventHolder(binding)


    }

    override fun onBindViewHolder(holder: CanceledEventHolder, position: Int) {
        holder.binding.tvEventName.text = cacneledEvents[position].name
        val startDate = LocalDate.parse(cacneledEvents[position].startDate,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
        val endDate = LocalDate.parse(cacneledEvents[position].endDate,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
        holder.binding.tvEventDates.text = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY)
                .format(startDate) + " - " +
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY).format(endDate)

        val cancelletaionDateTime = LocalDate.parse(cacneledEvents[position].canceledEventData!!.cancellationDateTime,
            DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DEFAULT))
        holder.binding.tvCancellationDateTime.text =
                "Canceled on: " + DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DISPLAY)
                        .format(cancelletaionDateTime)


    }

    override fun getItemCount(): Int  = cacneledEvents.size
}