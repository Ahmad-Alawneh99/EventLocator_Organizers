package com.eventlocator.eventlocatororganizers.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.eventlocator.eventlocatororganizers.adapters.PreviousEventAdapter
import com.eventlocator.eventlocatororganizers.data.Event
import com.eventlocator.eventlocatororganizers.databinding.FragmentPreviousEventsBinding

class PreviousEventsFragment(var events: ArrayList<Event>): Fragment() {

    lateinit var binding: FragmentPreviousEventsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPreviousEventsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = PreviousEventAdapter(events)
        binding.rvPreviousEvents.adapter = adapter
    }
}