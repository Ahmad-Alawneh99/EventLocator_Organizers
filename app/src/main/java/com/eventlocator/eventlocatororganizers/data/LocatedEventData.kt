package com.eventlocator.eventlocatororganizers.data

import com.eventlocator.eventlocatororganizers.utilities.City

data class LocatedEventData(var city: City, var location: Pair<Double, Double>) {
}