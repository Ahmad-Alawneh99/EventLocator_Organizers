package com.eventlocator.eventlocatororganizers.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.eventlocator.eventlocatororganizers.databinding.SessionStatisticsBinding

class SessionStatisticsAdapter(private val daysAndDates: ArrayList<String>, private val numbersOfParticipants: ArrayList<Int>,
                               private val averageArrivalTimes: ArrayList<String>):
        RecyclerView.Adapter<SessionStatisticsAdapter.SessionStatisticsHolder>() {

    lateinit var activity: OnSessionCheckChangeListener

    inner class SessionStatisticsHolder(var binding: SessionStatisticsBinding): RecyclerView.ViewHolder(binding.root){
        init{
            binding.cbIncludeSession.setOnCheckedChangeListener { buttonView, isChecked ->
                activity.onCheckChange()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionStatisticsHolder {
        val binding =SessionStatisticsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        activity = parent.context as OnSessionCheckChangeListener
        return SessionStatisticsHolder(binding)
    }

    override fun onBindViewHolder(holder: SessionStatisticsHolder, position: Int) {
        holder.setIsRecyclable(false)
        holder.binding.cbIncludeSession.text = daysAndDates[position]
        holder.binding.tvTotalParticipants.text = numbersOfParticipants[position].toString()
        holder.binding.tvAvgArrivalTime.text = averageArrivalTimes[position]
    }

    override fun getItemCount(): Int = daysAndDates.size
}

interface OnSessionCheckChangeListener{
    fun onCheckChange()
}