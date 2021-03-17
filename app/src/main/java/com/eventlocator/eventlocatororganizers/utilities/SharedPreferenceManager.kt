package com.eventlocator.eventlocatororganizers.utilities

class SharedPreferenceManager {
    val SHARED_PREFERENCE_FILE = "com.eventlocator.eventlocatororganizers"
    val TOKEN_KEY = "Token"
    val FIRST_TIME_KEY = "FirstTime?"
    companion object{
        val instance: SharedPreferenceManager = SharedPreferenceManager()
    }

}