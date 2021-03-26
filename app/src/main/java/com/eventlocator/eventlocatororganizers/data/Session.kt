package com.eventlocator.eventlocatororganizers.data

import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormat
import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormatterFactory
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class Session(var id: Int, var date: String, var startTime: String, var endTime: String, var dayOfWeek: Int, var checkInTime: String) {
    //TODO: Modify to support arabic
    override fun toString(): String {
        val date = LocalDate.parse(date, DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
        val startTime = LocalTime.parse(startTime, DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DEFAULT))
        val endTime = LocalTime.parse(endTime, DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DEFAULT))
        val checkInTime = if (checkInTime!="") LocalTime.parse(checkInTime,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DEFAULT))
                else null
        val timeFormatter = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DISPLAY)
        return "Date: " + DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY).format(date) + "\n" +
        "Start time: "+ timeFormatter.format(startTime) +
                "\nEnd time: " + timeFormatter.format(endTime) +
                if (checkInTime!=null) "\nCheck-in time: " + timeFormatter.format(checkInTime) else ""

    }
}