package com.eventlocator.eventlocatororganizers.data

import com.eventlocator.eventlocatororganizers.utilities.City
import com.google.android.gms.maps.model.LatLng

data class LocatedEventData(var city: Int, var location: ArrayList<Double>) {
}