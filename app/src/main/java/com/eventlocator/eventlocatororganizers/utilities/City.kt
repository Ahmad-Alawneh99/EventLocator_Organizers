package com.eventlocator.eventlocatororganizers.utilities

enum class City {
    AMMAN, ZARQA, BALQA, MADABA, IRBID, JERASH, MAFRAQ, AJLOUN, KARAK, AQABA, MAAN, TAFILA;
    companion object{
        fun getCityFromValue(value: Int): City? = City.values().find{it.ordinal == value}
    }
}