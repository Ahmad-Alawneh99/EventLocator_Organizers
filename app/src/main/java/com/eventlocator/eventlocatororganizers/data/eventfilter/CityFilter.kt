package com.eventlocator.eventlocatororganizers.data.eventfilter

import com.eventlocator.eventlocatororganizers.data.Event

class CityFilter(var cities: ArrayList<Int>): Filter {
    //Will only be called if located events are selected
    override fun apply(events: ArrayList<Event>): ArrayList<Event> {
        val result = ArrayList<Event>()
        for(i in 0 until events.size){
            if (events[i].locatedEventData==null || cities.contains(events[i].locatedEventData!!.city)){
                result.add(events[i])
            }
        }
        return result
    }

}