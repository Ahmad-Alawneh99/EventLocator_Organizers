package com.eventlocator.eventlocatororganizers.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.eventlocator.eventlocatororganizers.ui.CanceledEventsFragment
import com.eventlocator.eventlocatororganizers.ui.PreviousEventsFragment
import com.eventlocator.eventlocatororganizers.ui.UpcomingEventsFragment

class EventPagerAdapter(var context: Context, var fm:FragmentManager, var behaviour: Int): FragmentStatePagerAdapter(fm, behaviour) {
    override fun getCount(): Int {
        return behaviour
    }

    override fun getItem(position: Int): Fragment {
        when(position){
            0 -> return UpcomingEventsFragment()
            1 -> return PreviousEventsFragment()
            2 -> return CanceledEventsFragment()
        }
        return UpcomingEventsFragment()
    }
}