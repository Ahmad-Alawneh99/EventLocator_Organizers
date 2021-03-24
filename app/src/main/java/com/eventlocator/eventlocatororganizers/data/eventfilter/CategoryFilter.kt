package com.eventlocator.eventlocatororganizers.data.eventfilter

import com.eventlocator.eventlocatororganizers.data.Event
import java.util.*
import kotlin.collections.ArrayList

class CategoryFilter(var categories: ArrayList<Int>): Filter {

    override fun apply(events: ArrayList<Event>): ArrayList<Event> {
        val result = ArrayList<Event>()
        for(i in 0 until events.size){
            if (!Collections.disjoint(categories, events[i].categories)){
                result.add(events[i])
            }
        }
        return result
    }
}