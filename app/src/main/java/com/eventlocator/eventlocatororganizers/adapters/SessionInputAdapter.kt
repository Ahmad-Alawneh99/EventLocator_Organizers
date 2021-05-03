package com.eventlocator.eventlocatororganizers.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.databinding.SessionInputBinding
import com.eventlocator.eventlocatororganizers.ui.CreateEventActivity
import com.eventlocator.eventlocatororganizers.utilities.DateErrorUtil
import com.eventlocator.eventlocatororganizers.utilities.TimeStamp
import com.eventlocator.eventlocatororganizers.utilities.Utils
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

class SessionInputAdapter(var dates: ArrayList<String>, var isLimited: Boolean, val initialStartTime: TimeStamp,
val initialEndTime: TimeStamp, val initialCheckInTime: TimeStamp): RecyclerView.Adapter<SessionInputAdapter.SessionInputHolder>() {
    lateinit var context: Context
    lateinit var activityAsDateErrorUtil: DateErrorUtil

    inner class SessionInputHolder(var binding: SessionInputBinding): RecyclerView.ViewHolder(binding.root) {
        var startTime = TimeStamp()
        var endTime = TimeStamp()
        var checkInTime = TimeStamp()
        init {
            if (initialStartTime.hour >= 0){
                startTime = TimeStamp(initialStartTime.hour, initialStartTime.minute)
                binding.tvStartTime.text = startTime.format12H()
                if (initialEndTime.hour >= 0){
                    endTime = TimeStamp(initialEndTime.hour, initialEndTime.minute)
                    binding.tvEndTime.text = endTime.format12H()
                    if (initialCheckInTime.hour >= 0){
                        checkInTime = TimeStamp(initialCheckInTime.hour, initialCheckInTime.minute)
                        binding.tvCheckInTime.text = checkInTime.format12H()
                    }
                    else{
                        binding.btnCheckInTime.isEnabled = false
                    }
                }
                else{
                    binding.btnEndTime.isEnabled = false
                    binding.btnCheckInTime.isEnabled = false
                }
            }
            else{
                binding.btnEndTime.isEnabled = false
                binding.btnCheckInTime.isEnabled = false
            }

            binding.cbEnableSession.isChecked = true
            binding.btnStartTime.setOnClickListener {
                val picker = MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_12H)
                        .setHour(12)
                        .setMinute(0)
                        .setTitleText("Select start time")
                        .build()
                picker.addOnPositiveButtonClickListener {
                    startTime = TimeStamp(picker.hour, picker.minute)
                    binding.tvStartTime.text = startTime.format12H()
                    binding.btnEndTime.isEnabled = true
                    binding.tvEndTime.text = context.getString(R.string.select_time)
                    binding.tvCheckInTime.text = context.getString(R.string.select_time)
                    endTime = TimeStamp(-1,-1)
                    checkInTime = TimeStamp(-1,-1)
                    setLimited(isLimited)
                    activityAsDateErrorUtil.setDateError()
                }

                picker.show((context as AppCompatActivity).supportFragmentManager, "sessionStartTime")

            }

            binding.btnEndTime.setOnClickListener {
                val picker = MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_12H)
                        .setHour(12)
                        .setMinute(0)
                        .setTitleText("Select end time")
                        .build()
                picker.addOnPositiveButtonClickListener {
                    if (TimeStamp(picker.hour, picker.minute).minusInMinutes(startTime) > 12*60){
                        AlertDialog.Builder(context)
                                .setTitle(context.getString(R.string.time_error))
                                .setMessage(context.getString(R.string.session_time_limit_error))
                                .setPositiveButton(context.getString(R.string.ok)){ dialogInterface: DialogInterface, i: Int -> }
                                .create().show()
                    }
                    else if (TimeStamp(picker.hour, picker.minute).minusInMinutes(startTime) < 0){
                        AlertDialog.Builder(context)
                                .setTitle(context.getString(R.string.time_error))
                                .setMessage(context.getString(R.string.end_time_before_start_time_error))
                                .setPositiveButton(context.getString(R.string.ok)){ dialogInterface: DialogInterface, i: Int -> }
                                .create().show()
                    }
                    else {
                        endTime = TimeStamp(picker.hour, picker.minute)
                        binding.tvEndTime.text = endTime.format12H()
                        activityAsDateErrorUtil.setDateError()
                    }
                }

                picker.show((context as AppCompatActivity).supportFragmentManager, "sessionEndTime")

            }


            binding.btnCheckInTime.setOnClickListener {
                val picker = MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_12H)
                        .setHour(12)
                        .setMinute(0)
                        .setTitleText("Select check-in time")
                        .build()
                picker.addOnPositiveButtonClickListener {
                    if (startTime.minusInMinutes(TimeStamp(picker.hour, picker.minute)) > 3 * 60){
                        AlertDialog.Builder(context)
                                .setTitle(context.getString(R.string.time_error))
                                .setMessage(context.getString(R.string.check_in_time_limit_error))
                                .setPositiveButton(context.getString(R.string.ok)){ dialogInterface: DialogInterface, i: Int -> }
                                .create().show()
                    }else if (startTime.minusInMinutes(TimeStamp(picker.hour, picker.minute)) < 0){
                        AlertDialog.Builder(context)
                                .setTitle(context.getString(R.string.time_error))
                                .setMessage(context.getString(R.string.check_in_time_before_start_time_error))
                                .setPositiveButton(context.getString(R.string.ok)){ dialogInterface: DialogInterface, i: Int -> }
                                .create().show()
                    }else{
                        checkInTime = TimeStamp(picker.hour, picker.minute)
                        binding.tvCheckInTime.text = checkInTime.format12H()
                    }

                }

                picker.show((context as AppCompatActivity).supportFragmentManager, "sessionStartTime")
            }

            binding.cbEnableSession.setOnCheckedChangeListener { buttonView, isChecked ->
                if (!isChecked){
                    binding.btnStartTime.isEnabled = false
                    binding.btnEndTime.isEnabled = false
                    binding.btnCheckInTime.isEnabled = false
                }
                else{
                    binding.btnStartTime.isEnabled = true
                    if (binding.tvStartTime.text.toString() != context.getString(R.string.select_time)){
                        binding.btnEndTime.isEnabled = true
                        setLimited(isLimited)
                    }
                }
                activityAsDateErrorUtil.setDateError()
            }
        }

        fun setLimited(b: Boolean){
            isLimited = b
            if (b){
                if (binding.btnEndTime.isEnabled){
                    binding.btnCheckInTime.isEnabled = true
                }
            }
            else{
                binding.btnCheckInTime.isEnabled = false
                binding.tvCheckInTime.text = context.getString(R.string.select_time)
                checkInTime = TimeStamp(-1,-1)
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionInputHolder {
        val binding = SessionInputBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        activityAsDateErrorUtil = parent.context as DateErrorUtil
        return SessionInputHolder(binding)
    }

    override fun onBindViewHolder(holder: SessionInputHolder, position: Int) {
        holder.binding.cbEnableSession.text = dates[position]
        holder.binding.cbEnableSession.isEnabled = position != dates.size - 1

    }
    override fun getItemCount(): Int {
        return dates.size
    }


}