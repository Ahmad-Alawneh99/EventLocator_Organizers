package com.eventlocator.eventlocatororganizers.ui

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import androidx.core.util.Pair
import com.eventlocator.eventlocatororganizers.databinding.ActivityCreateEventBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class CreateEventActivity : AppCompatActivity() {
    lateinit var binding: ActivityCreateEventBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSelectDates.setOnClickListener {
            var builder: MaterialDatePicker.Builder<Pair<Long, Long>> = MaterialDatePicker.Builder.dateRangePicker()

            var calendarConstraints = CalendarConstraints.Builder()
            var startConstraint = LocalDate.now()
            var min = startConstraint.plusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            calendarConstraints.setStart(min)
            calendarConstraints.setValidator(DateValidatorPointForward.from(min))
            builder.setCalendarConstraints(calendarConstraints.build())
            builder.setTitleText("Select start and end dates (max 7 days)")
            var picker = builder.build()
            picker.addOnPositiveButtonClickListener {
                if (it.first!=null) {
                    var dt: LocalDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(it.first!!),
                        ZoneId.systemDefault()).toLocalDate()
                    binding.tvStartDate.text = dt.toString()

                }
                if (it.second!=null) {
                    var dt: LocalDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(it.second!!),
                        ZoneId.systemDefault()).toLocalDate()
                    binding.tvEndDate.text = dt.toString()
                }
            }
            picker.show(supportFragmentManager, builder.build().toString())

        }
    }
}