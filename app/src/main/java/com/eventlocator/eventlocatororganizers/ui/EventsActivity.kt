package com.eventlocator.eventlocatororganizers.ui

import com.eventlocator.eventlocatororganizers.adapters.EventPagerAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.databinding.ActivityEventsBinding
import com.google.android.material.tabs.TabLayout

class EventsActivity : AppCompatActivity(){
    lateinit var binding: ActivityEventsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tlEvents.addTab(binding.tlEvents.newTab().setText(R.string.upcoming_events))
        binding.tlEvents.addTab(binding.tlEvents.newTab().setText(R.string.previous_events))
        binding.tlEvents.addTab(binding.tlEvents.newTab().setText(R.string.canceled_events))
        binding.tlEvents.tabGravity = TabLayout.GRAVITY_FILL

        val pagerAdapter = EventPagerAdapter(this, supportFragmentManager, binding.tlEvents.tabCount)
        binding.pagerEvents.adapter = pagerAdapter
        binding.pagerEvents.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(binding.tlEvents))
        binding.tlEvents.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    binding.pagerEvents.currentItem = tab.position
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

    }
}