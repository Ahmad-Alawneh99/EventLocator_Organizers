package com.eventlocator.eventlocatororganizers.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.eventlocator.eventlocatororganizers.adapters.PreviousEventAdapter
import com.eventlocator.eventlocatororganizers.data.Event
import com.eventlocator.eventlocatororganizers.databinding.FragmentPreviousEventsBinding

class PreviousEventsFragment(var events: ArrayList<Event>): Fragment(), OnPreviousEventsReadyListener {
    lateinit var binding: FragmentPreviousEventsBinding

    constructor(): this(ArrayList<Event>())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPreviousEventsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvPreviousEvents.layoutManager = layoutManager
        binding.rvPreviousEvents.addItemDecoration(DividerItemDecoration(requireContext(),layoutManager.orientation))
        val adapter = PreviousEventAdapter(events)
        binding.rvPreviousEvents.adapter = adapter
        (activity as EventsActivity).onPreviousEventsReadyListener = this
    }

    override fun getResult(events: ArrayList<Event>) {
        this.events = events
        val adapter = PreviousEventAdapter(events)
        binding.rvPreviousEvents.adapter = adapter
        binding.rvPreviousEvents.adapter!!.notifyDataSetChanged()
    }
}