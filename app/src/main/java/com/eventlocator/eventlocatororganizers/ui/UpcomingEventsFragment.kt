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

    constructor(): this(ArrayList<Event>())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentUpcomingEventsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvUpcomingEvents.layoutManager = layoutManager

        val adapter = UpcomingEventAdapter(events)
        binding.rvUpcomingEvents.adapter = adapter
        binding.rvUpcomingEvents.adapter!!.notifyDataSetChanged()
    }
}