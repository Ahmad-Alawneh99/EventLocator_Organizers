package com.eventlocator.eventlocatororganizers.adapters


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.eventlocator.eventlocatororganizers.data.Event
import com.eventlocator.eventlocatororganizers.ui.CanceledEventsFragment
import com.eventlocator.eventlocatororganizers.ui.PreviousEventsFragment
import com.eventlocator.eventlocatororganizers.ui.UpcomingEventsFragment
import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormat
import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormatterFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class EventPagerAdapter(fa: FragmentActivity, var numberOfTabs: Int, events: ArrayList<Event>): FragmentStateAdapter(fa) {
    private var upcomingEvents = ArrayList<Event>()
    var previousEvents = ArrayList<Event>()
    private var canceledEvents = ArrayList<Event>()
    init{
        filterEvents(events)
    }
    override fun getItemCount(): Int {
        return numberOfTabs
    }

    override fun createFragment(position: Int): Fragment {
        when(position){
            0 -> return UpcomingEventsFragment(upcomingEvents)
            1 -> return PreviousEventsFragment(previousEvents)
            2 -> return CanceledEventsFragment(canceledEvents)
        }
        return UpcomingEventsFragment(upcomingEvents)
    }

    private fun filterEvents(events: ArrayList<Event>){
        for(i in 0 until events.size){
            if (events[i].isCanceled()){
                canceledEvents.add(events[i])
            }
            else if (events[i].isFinished()){
                previousEvents.add(events[i])
            }
            else{
                upcomingEvents.add(events[i])
            }
        }
    }
}