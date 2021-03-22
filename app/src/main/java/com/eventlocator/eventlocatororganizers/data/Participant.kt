package com.eventlocator.eventlocatororganizers.data

data class Participant(var id: Long, var firstName: String, var lastName: String, var rating: Double, var arrivalTime: String) {

    override fun toString(): String {
        return "$firstName $lastName\n" +
                if (rating> 0) "Rating: $rating" else ""
    }
}