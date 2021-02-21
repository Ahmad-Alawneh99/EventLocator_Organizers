package com.eventlocator.eventlocatororganizers.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class Session(var id: Int, var date: LocalDate, var startTime: LocalTime, var endTime: LocalTime, var dayOfWeek: DayOfWeek, var checkInTime: LocalTime) {
}