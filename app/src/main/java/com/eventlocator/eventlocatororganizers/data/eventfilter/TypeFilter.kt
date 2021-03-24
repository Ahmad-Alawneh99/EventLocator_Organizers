package com.eventlocator.eventlocatororganizers.data.eventfilter

import com.eventlocator.eventlocatororganizers.data.Event

class TypeFilter(var types: ArrayList<Int>): Filter {

    override fun apply(events: ArrayList<Event>): ArrayList<Event> {
        return if (types.size == 2) events
        else{
            val result = ArrayList<Event>()
            for(i in 0 until events.size){
                if(if(types[0]==0)events[i].locatedEventData==null
                    else events[i].locatedEventData!=null){
                    result.add(events[i])
                }
            }
            result
        }
    }


}