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
import com.eventlocator.eventlocatororganizers.utilities.Utils
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

class SessionInputAdapter(var dates: ArrayList<String>, var isLimited: Boolean): RecyclerView.Adapter<SessionInputAdapter.SessionInputHolder>() {
    lateinit var context: Context

    inner class SessionInputHolder(var binding: SessionInputBinding): RecyclerView.ViewHolder(binding.root) {
        var startTimeHour: Int = -1
        var startTimeMinute: Int = -1
        var endTimeHour: Int = -1
        var endTimeMinute: Int = -1
        var checkInTimeHour = -1
        var checkInTimeMinute = -1
        init {

            binding.cbEnableSession.isChecked = true
            binding.btnEndTime.isEnabled = false
            binding.btnCheckInTime.isEnabled = false
            binding.btnStartTime.setOnClickListener {
                val picker = MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_12H)
                        .setHour(12)
                        .setMinute(10)
                        .setTitleText("Select start time")
                        .build()
                picker.addOnPositiveButtonClickListener {
                    //TODO: Handle pm or fm
                    binding.tvStartTime.text = "${picker.hour} ${picker.minute}"
                    startTimeHour = picker.hour
                    startTimeMinute = picker.minute
                    binding.btnEndTime.isEnabled = true
                    binding.tvEndTime.text = context.getString(R.string.select_time)
                    binding.tvCheckInTime.text = context.getString(R.string.select_time)
                    setLimited(isLimited)
                }

                picker.show((context as AppCompatActivity).supportFragmentManager, "sessionStartTime")

            }

            binding.btnEndTime.setOnClickListener {
                val picker = MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_12H)
                        .setHour(12)
                        .setMinute(10)
                        .setTitleText("Select end time")
                        .build()
                picker.addOnPositiveButtonClickListener {
                    //TODO: Handle pm or fm
                    if (Utils.instance.differenceBetweenTimesInMinutes(startTimeHour, startTimeMinute, picker.hour, picker.minute) > 12*60){
                        AlertDialog.Builder(context)
                                .setTitle(context.getString(R.string.time_error))
                                .setMessage(context.getString(R.string.session_time_limit_error))
                                .setPositiveButton(context.getString(R.string.ok)){ dialogInterface: DialogInterface, i: Int -> }
                                .create().show()
                    }
                    else if (Utils.instance.differenceBetweenTimesInMinutes(startTimeHour, startTimeMinute, picker.hour, picker.minute) < 0){
                        AlertDialog.Builder(context)
                                .setTitle(context.getString(R.string.time_error))
                                .setMessage(context.getString(R.string.end_time_before_start_time_error))
                                .setPositiveButton(context.getString(R.string.ok)){ dialogInterface: DialogInterface, i: Int -> }
                                .create().show()
                    }
                    else {
                        binding.tvEndTime.text = "${picker.hour} ${picker.minute}"
                        endTimeHour = picker.hour
                        endTimeMinute = picker.minute

                    }
                }

                picker.show((context as AppCompatActivity).supportFragmentManager, "sessionEndTime")

            }


            binding.btnCheckInTime.setOnClickListener {
                val picker = MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_12H)
                        .setHour(12)
                        .setMinute(10)
                        .setTitleText("Select start time")
                        .build()
                picker.addOnPositiveButtonClickListener {
                    //TODO: Handle pm or fm
                    //11 hours 59 mins
                    if (Utils.instance.differenceBetweenTimesInMinutes(picker.hour, picker.minute, startTimeHour, startTimeMinute) > 3 * 60 - 1){
                        AlertDialog.Builder(context)
                                .setTitle(context.getString(R.string.time_error))
                                .setMessage(context.getString(R.string.check_in_time_limit_error))
                                .setPositiveButton(context.getString(R.string.ok)){ dialogInterface: DialogInterface, i: Int -> }
                                .create().show()
                    }else if (Utils.instance.differenceBetweenTimesInMinutes(picker.hour, picker.minute, startTimeHour, startTimeMinute) < 0){
                        AlertDialog.Builder(context)
                                .setTitle(context.getString(R.string.time_error))
                                .setMessage(context.getString(R.string.check_in_time_before_start_time_error))
                                .setPositiveButton(context.getString(R.string.ok)){ dialogInterface: DialogInterface, i: Int -> }
                                .create().show()
                    }else{
                        binding.tvCheckInTime.text = "${picker.hour} ${picker.minute}"
                        checkInTimeHour = picker.hour
                        checkInTimeMinute = picker.minute
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
                //TODO: remove data from text view
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionInputHolder {
        val binding = SessionInputBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return SessionInputHolder(binding)
    }

    override fun onBindViewHolder(holder: SessionInputHolder, position: Int) {

        holder.binding.cbEnableSession.text = dates[position]
        holder.binding.cbEnableSession.isEnabled = !(position ==0 || position == dates.size - 1)


    }
    override fun getItemCount(): Int {
        return dates.size
    }


}