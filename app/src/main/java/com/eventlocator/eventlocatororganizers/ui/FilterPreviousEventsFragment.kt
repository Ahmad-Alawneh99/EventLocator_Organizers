package com.eventlocator.eventlocatororganizers.ui

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Pair
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.Fragment
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.data.Event
import com.eventlocator.eventlocatororganizers.data.eventfilter.*
import com.eventlocator.eventlocatororganizers.databinding.FragmentFilterPreviousEventsBinding
import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormat
import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormatterFactory
import com.eventlocator.eventlocatororganizers.utilities.TimeStamp
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.*
import java.time.format.DateTimeFormatter

class FilterPreviousEventsFragment(var events: ArrayList<Event>): Fragment() {

    lateinit var binding:FragmentFilterPreviousEventsBinding
    lateinit var activity: OnEventsFiltered
    var startDate: LocalDate? = null
    var endDate: LocalDate? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFilterPreviousEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnApply.setOnClickListener {
            val selectedTypes = ArrayList<Int>()
            val selectedCategories = ArrayList<Int>()
            val selectedCities = ArrayList<Int>()

            for(i in 0 until binding.cgType.size){
                if ((binding.cgType[i] as Chip).isChecked){
                    selectedTypes.add(i)
                }
            }


            for(i in 0 until binding.cgCategory.size){
                if ((binding.cgCategory[i] as Chip).isChecked){
                    selectedCategories.add(i)
                }
            }

            for(i in 0 until binding.cgCity.size){
                if ((binding.cgCity[i] as Chip).isChecked){
                    selectedCities.add(i)
                }
            }

            var filter: Filter = TypeFilter(selectedTypes)
            var result = filter.apply(events)
            filter = CategoryFilter(selectedCategories)
            result = filter.apply(result)
            filter = CityFilter(selectedCities)
            result = filter.apply(result)
            if (startDate!=null){
                filter = DatePeriodFilter(startDate!!, endDate!!)
                result = filter.apply(result)
            }

            activity.getFilteredResult(result)

        }

        binding.cgType.setOnCheckedChangeListener { group, checkedId ->
                binding.cgCity.isEnabled = binding.cLocated.isChecked
        }

        binding.btnSelectDates.setOnClickListener {
            val builder: MaterialDatePicker.Builder<Pair<Long, Long>> = MaterialDatePicker.Builder.dateRangePicker()

            val calendarConstraints = CalendarConstraints.Builder()
            val endConstraint = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
            calendarConstraints.setEnd(endConstraint)
            calendarConstraints.setValidator(DateValidatorPointBackward.before(endConstraint))
            builder.setCalendarConstraints(calendarConstraints.build())
            builder.setTitleText("Select start and end dates")
            val picker = builder.build()
            picker.addOnPositiveButtonClickListener {
                val from: LocalDate = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(it.first!!),
                    ZoneId.systemDefault()).toLocalDate()
                val to: LocalDate = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(it.second!!),
                    ZoneId.systemDefault()).toLocalDate()

                startDate = from
                endDate = to

                binding.tvStartDate.text = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY)
                    .format(startDate)

                binding.tvEndDate.text = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY)
                    .format(endDate)

            }
            picker.show(parentFragmentManager, builder.build().toString())
        }

        binding.btnRemoveDates.setOnClickListener {
            startDate = null
            endDate = null
            binding.tvStartDate.text = getString(R.string.select_date)
            binding.tvEndDate.text = getString(R.string.select_date)
        }


    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as OnEventsFiltered
    }
}

interface OnEventsFiltered{
    fun getFilteredResult(events: ArrayList<Event>)
}