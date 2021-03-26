package com.eventlocator.eventlocatororganizers.utilities

class TimeStamp(var hour: Int, var minute: Int) {

    constructor(): this(0,0)

    fun minusInMinutes(ts: TimeStamp): Int{
        var tempH1 = this.hour
        var tempM1 = this.minute
        if (this.minute < ts.minute){
            tempH1--
            tempM1+=60
        }
        return ((tempH1 - ts.hour) * 60) + tempM1 - ts.minute
    }

    fun format12H(): String{
        //TODO: Make it work for Arabic
        val isPM = hour>12
        val tempH = if (isPM)hour-12 else hour
        val h = if(tempH<10)"0"+tempH.toString() else tempH.toString()
        val m = if (minute<10)"0"+minute.toString() else minute

        return h+':'+m+' '+ if(!isPM && tempH!=12)"AM" else "PM"
    }

    fun format24H(): String{
        val h = if (this.hour<10) "0"+hour.toString() else hour.toString()
        val m = if (this.minute < 10) "0"+minute.toString() else minute.toString()

        return h+':'+m

    }

    fun minusMinutes(minutes: Int): TimeStamp{
        var i = minutes
        var newMinute = this.minute
        var newHour = this.hour
        while(i--> 0){
            if (newMinute == 0){
                if (newHour == 0){
                    newHour = 23
                }
                else newHour --
                newMinute = 60
            }
            newMinute --
        }
        return TimeStamp(newHour, newMinute)
    }
}