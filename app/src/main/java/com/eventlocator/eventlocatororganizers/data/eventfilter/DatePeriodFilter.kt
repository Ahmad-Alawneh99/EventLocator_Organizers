package com.eventlocator.eventlocatororganizers.data.eventfilter

import com.eventlocator.eventlocatororganizers.data.Event
import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormat
import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormatterFactory
import java.time.LocalDate

class DatePeriodFilter(var startDate: LocalDate, var endDate: LocalDate): Filter {

    override fun apply(events: ArrayList<Event>): ArrayList<Event> {
        val result = ArrayList<Event>()
        for(i in 0 until events.size){
            val eventStartDate = LocalDate.parse(events[i].startDate,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))

            if (eventStartDate.isAfter(startDate) && eventStartDate.isBefore(endDate)){
                result.add(events[i])
            }
        }
        return result
    }


}