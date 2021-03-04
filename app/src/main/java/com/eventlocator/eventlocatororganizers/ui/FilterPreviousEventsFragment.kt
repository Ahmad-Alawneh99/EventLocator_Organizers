package com.eventlocator.eventlocatororganizers.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.eventlocator.eventlocatororganizers.data.Event
import com.eventlocator.eventlocatororganizers.databinding.FragmentFilterPreviousEventsBinding

class FilterPreviousEventsFragment(var eventList: ArrayList<Event>): Fragment() {

    lateinit var binding:FragmentFilterPreviousEventsBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFilterPreviousEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }
}