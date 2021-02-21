package com.eventlocator.eventlocatororganizers.data

import java.time.LocalDateTime

data class CanceledEventData(var cancellationDateTime: LocalDateTime, var cancellationReason: String) {
}