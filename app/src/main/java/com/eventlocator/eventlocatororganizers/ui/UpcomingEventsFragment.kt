package com.eventlocator.eventlocatororganizers.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.adapters.UpcomingEventAdapter
import com.eventlocator.eventlocatororganizers.data.Event
import com.eventlocator.eventlocatororganizers.databinding.FragmentUpcomingEventsBinding
import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormat
import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormatterFactory
import com.eventlocator.eventlocatororganizers.utilities.EventStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class UpcomingEventsFragment(var events: ArrayList<Event>): Fragment() {

    lateinit var binding: FragmentUpcomingEventsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentUpcomingEventsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val status = ArrayList<String>()
        for(i in 0 until events.size){
            if (events[i].status == EventStatus.PENDING.ordinal){
                status.add(getString(R.string.pending))
            }
            else{
                val registrationCloseDateTime =
                        LocalDateTime.parse(events[i].registrationCloseDateTime,
                                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DEFAULT))
                val startDate = LocalDate.parse(events[i].startDate,
                        DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
                val startDateTime = startDate.atTime(LocalTime.parse(events[i].sessions[0].startTime,
                        DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DEFAULT)))

                if (LocalDateTime.now().isBefore(registrationCloseDateTime)){
                    status.add(getString(R.string.registration_ongoing))
                }
                else if (LocalDateTime.now().isBefore(startDateTime) && LocalDateTime.now().isAfter(registrationCloseDateTime)){
                    status.add(getString(R.string.registration_closed))
                }
                else{
                    var found = false
                    for(j in 0 until events[i].sessions.size){
                        val sessionDate = LocalDate.parse(events[i].sessions[j].date,
                                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
                        val sessionStartDateTime = sessionDate.atTime(LocalTime.parse(events[i].sessions[j].startTime,
                                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DEFAULT)))
                        val sessionEndDateTime = sessionDate.atTime(LocalTime.parse(events[i].sessions[j].endTime,
                                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DEFAULT)))

                        if (LocalDateTime.now().isAfter(sessionStartDateTime) && LocalDateTime.now().isBefore(sessionEndDateTime)){
                            status.add(getString(R.string.session_happening_right_now))
                            found = true
                            break;
                        }
                    }

                    if (!found){
                        status.add(getString(R.string.active))
                    }
                }
            }
        }
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvUpcomingEvents.layoutManager = layoutManager
        binding.rvUpcomingEvents.addItemDecoration(DividerItemDecoration(requireContext(),layoutManager.orientation))

        val adapter = UpcomingEventAdapter(events, status)
        binding.rvUpcomingEvents.adapter = adapter
        binding.rvUpcomingEvents.adapter!!.notifyDataSetChanged()
    }
}