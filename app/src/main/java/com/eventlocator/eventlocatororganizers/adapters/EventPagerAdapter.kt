package com.eventlocator.eventlocatororganizers.adapters


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.eventlocator.eventlocatororganizers.ui.CanceledEventsFragment
import com.eventlocator.eventlocatororganizers.ui.PreviousEventsFragment
import com.eventlocator.eventlocatororganizers.ui.UpcomingEventsFragment

class EventPagerAdapter(fa: FragmentActivity, var numberOfTabs: Int): FragmentStateAdapter(fa) {

    override fun getItemCount(): Int {
        return numberOfTabs
    }

    override fun createFragment(position: Int): Fragment {
        when(position){
            0 -> return UpcomingEventsFragment()
            1 -> return PreviousEventsFragment()
            2 -> return CanceledEventsFragment()
        }
        return UpcomingEventsFragment()
    }
}