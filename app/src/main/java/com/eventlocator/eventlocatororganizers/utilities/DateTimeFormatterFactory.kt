package com.eventlocator.eventlocatororganizers.utilities

import java.time.format.DateTimeFormatter

class DateTimeFormatterFactory {

    companion object{
        fun createDateTimeFormatter(format: DateTimeFormat): DateTimeFormatter{
            return when(format){
                DateTimeFormat.DATE_DEFAULT-> DateTimeFormatter.ofPattern("yyyy-MM-dd")
                DateTimeFormat.TIME_DEFAULT-> DateTimeFormatter.ofPattern("HH:mm:ss")
                DateTimeFormat.DATE_TIME_DEFAULT-> DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                DateTimeFormat.DATE_DISPLAY-> DateTimeFormatter.ofPattern("dd/MM/yyyy")
                DateTimeFormat.TIME_DISPLAY-> DateTimeFormatter.ofPattern("hh:mm a")
                DateTimeFormat.DATE_TIME_DISPLAY-> DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")
            }
        }
    }
}