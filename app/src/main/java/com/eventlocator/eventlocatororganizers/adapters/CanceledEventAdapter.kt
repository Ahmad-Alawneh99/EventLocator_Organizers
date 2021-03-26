package com.eventlocator.eventlocatororganizers.adapters



import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.eventlocator.eventlocatororganizers.data.Event
import com.eventlocator.eventlocatororganizers.databinding.CanceledEventBinding
import com.eventlocator.eventlocatororganizers.ui.ViewEventActivity
import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormat
import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormatterFactory
import java.time.LocalDate
import java.time.LocalDateTime

class CanceledEventAdapter(private val cacneledEvents: ArrayList<Event>):
        RecyclerView.Adapter<CanceledEventAdapter.CanceledEventHolder>() {
    lateinit var context: Context
    inner class CanceledEventHolder(var binding: CanceledEventBinding): RecyclerView.ViewHolder(binding.root){
        init{
            binding.root.setOnClickListener {
                val intent = Intent(context, ViewEventActivity::class.java)
                intent.putExtra("eventID", binding.tvEventID.text.toString().toInt())
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CanceledEventHolder {
        val binding = CanceledEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return CanceledEventHolder(binding)


    }

    override fun onBindViewHolder(holder: CanceledEventHolder, position: Int) {
        holder.binding.tvEventID.text = cacneledEvents[position].id.toString()
        holder.binding.tvEventName.text = cacneledEvents[position].name
        val startDate = LocalDate.parse(cacneledEvents[position].startDate,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
        val endDate = LocalDate.parse(cacneledEvents[position].endDate,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
        holder.binding.tvEventDates.text = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY)
                .format(startDate) + " - " +
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY).format(endDate)

        val cancellationDateTime = LocalDateTime.parse(cacneledEvents[position].canceledEventData!!.cancellationDateTime,
            DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DEFAULT))
        holder.binding.tvCancellationDateTime.text =
                "Canceled on: " + DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DISPLAY)
                        .format(cancellationDateTime)


    }

    override fun getItemCount(): Int  = cacneledEvents.size
}