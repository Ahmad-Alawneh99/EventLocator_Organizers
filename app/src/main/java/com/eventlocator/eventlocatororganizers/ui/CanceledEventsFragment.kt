package com.eventlocator.eventlocatororganizers.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.eventlocator.eventlocatororganizers.adapters.CanceledEventAdapter
import com.eventlocator.eventlocatororganizers.data.Event
import com.eventlocator.eventlocatororganizers.databinding.FragmentCanceledEventsBinding

class CanceledEventsFragment(var events: ArrayList<Event>): Fragment() {
    lateinit var binding: FragmentCanceledEventsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCanceledEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = CanceledEventAdapter(events)
        binding.rvCanceledEvents.adapter = adapter

    }
}