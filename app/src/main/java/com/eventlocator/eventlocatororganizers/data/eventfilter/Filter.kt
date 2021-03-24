package com.eventlocator.eventlocatororganizers.data.eventfilter

import com.eventlocator.eventlocatororganizers.data.Event

interface Filter {

    fun apply(events: ArrayList<Event>): ArrayList<Event>
}