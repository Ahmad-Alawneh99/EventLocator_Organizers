package com.eventlocator.eventlocatororganizers.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class Session(var id: Int, var date: String, var startTime: String, var endTime: String, var dayOfWeek: DayOfWeek, var checkInTime: String) {

}