package com.eventlocator.eventlocatororganizers.data

import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormat
import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormatterFactory
import java.io.Serializable
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class Session(var id: Int, var date: String, var startTime: String, var endTime: String, var dayOfWeek: Int, var checkInTime: String): Serializable {

}