package com.eventlocator.eventlocatororganizers.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.eventlocator.eventlocatororganizers.adapters.CanceledEventAdapter
import com.eventlocator.eventlocatororganizers.data.Event
import com.eventlocator.eventlocatororganizers.databinding.FragmentCanceledEventsBinding

class CanceledEventsFragment(var events: ArrayList<Event>): Fragment() {
    lateinit var binding: FragmentCanceledEventsBinding

    constructor(): this(ArrayList<Event>())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCanceledEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvCanceledEvents.layoutManager = layoutManager
        val adapter = CanceledEventAdapter(events)
        binding.rvCanceledEvents.adapter = adapter

    }
}