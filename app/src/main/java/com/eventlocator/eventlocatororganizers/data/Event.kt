package com.eventlocator.eventlocatororganizers.data

import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormat
import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormatterFactory
import com.eventlocator.eventlocatororganizers.utilities.EventCategory
import com.eventlocator.eventlocatororganizers.utilities.EventStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class Event(var id: Long, var name: String, var description: String, var categories: ArrayList<Int>, var startDate: String,
            var endDate: String, var registrationCloseDateTime: String, var status: Int, var maxParticipants: Int,
            var rating: Double, var sessions: ArrayList<Session>, var locatedEventData: LocatedEventData?,
            var canceledEventData: CanceledEventData?, var image: String, var whatsAppLink: String?,var currentNumberOfParticipants: Int) {

    //called only when creating new events.
    private constructor(eventBuilder: EventBuilder) : this(-1, eventBuilder.name, eventBuilder.description, eventBuilder.categories,
                        eventBuilder.startDate, eventBuilder.endDate, eventBuilder.registrationCloseDateTime,
                        EventStatus.PENDING.ordinal, eventBuilder.maxParticipants, -1.0, eventBuilder.sessions,
                        eventBuilder.locatedEventData, null, "", eventBuilder.whatsAppLink, 0)


    class EventBuilder(var name: String, var description: String, var categories: ArrayList<Int>,
                       var startDate: String, var endDate: String, var sessions: ArrayList<Session>,
                       var registrationCloseDateTime: String){
        var maxParticipants: Int = -1
        var whatsAppLink: String = ""
        var locatedEventData: LocatedEventData? = null

        fun setMaxParticipants(value: Int): EventBuilder{
            this.maxParticipants = value
            return this
        }


        fun setLocatedEventData(locatedEventData: LocatedEventData?): EventBuilder{
            this.locatedEventData = locatedEventData
            return this
        }

        fun setWhatsAppLink(whatsAppLink: String): EventBuilder{
            this.whatsAppLink = whatsAppLink
            return this
        }

        fun build(): Event{
            return Event(this)
        }
    }

    fun getStatus(): String{
        return if (isCanceled()){
            "Canceled"
        }
        else if (status == EventStatus.PENDING.ordinal){
            "Pending (waiting for response from admins)"
        }
        else if (isFinished()){
            "Finished"
        }
        else if (!isRegistrationClosed()){
            if (isFull()){
                return "Full"
            }
            else{
                return "Registration ongoing"
            }
        }
        else if (isRegistrationClosed()){
            "Registration closed"
        }
        else if (getCurrentSession()!=null){
            "Session #"+getCurrentSession()!!.id+" is happening now"
        }
        else{
            "Active"
        }
    }

    private fun isFull(): Boolean {
        return this.maxParticipants!=-1 && this.maxParticipants == this.currentNumberOfParticipants
    }


    fun isCanceled(): Boolean = canceledEventData != null


    fun isRegistrationClosed(): Boolean {
        val registrationCloseDateTime =
                LocalDateTime.parse(registrationCloseDateTime,
                        DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DEFAULT))
        return LocalDateTime.now().isAfter(registrationCloseDateTime)
    }

    fun isFinished(): Boolean {
        val eventEndDate = LocalDate.parse(endDate, DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
        val eventEndDateTime = eventEndDate.atTime(LocalTime.parse(sessions[sessions.size-1].endTime,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DEFAULT)))

        return LocalDateTime.now().isAfter(eventEndDateTime)
    }

    fun hasStarted(): Boolean{
        val eventStartDate = LocalDate.parse(startDate,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
        val eventStartDateTime = eventStartDate.atTime(LocalTime.parse(sessions[0].startTime,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DEFAULT)))
        return LocalDateTime.now().isAfter(eventStartDateTime)
    }

    fun isLimitedLocated():Boolean{
        return maxParticipants!=-1 && locatedEventData!=null
    }

    fun getCurrentSession(): Session? {
        for(j in 0 until sessions.size) {
            val sessionDate = LocalDate.parse(sessions[j].date,
                    DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
            val sessionStartDateTime = sessionDate.atTime(LocalTime.parse(sessions[j].startTime,
                    DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DEFAULT)))
            val sessionEndDateTime = sessionDate.atTime(LocalTime.parse(sessions[j].endTime,
                    DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DEFAULT)))
            if (LocalDateTime.now().isAfter(sessionStartDateTime) && LocalDateTime.now().isBefore(sessionEndDateTime)) {
                return sessions[j]
            }
        }
        return null
    }

    fun getCurrentLimitedSessionIncludingCheckInTime(): Session?{
        for(j in 0 until sessions.size) {
            val sessionDate = LocalDate.parse(sessions[j].date,
                    DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
            val sessionCheckInDateTime = sessionDate.atTime(LocalTime.parse(sessions[j].checkInTime,
                    DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DEFAULT)))
            val sessionEndDateTime = sessionDate.atTime(LocalTime.parse(sessions[j].endTime,
                    DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DEFAULT)))
            if (LocalDateTime.now().isAfter(sessionCheckInDateTime) && LocalDateTime.now().isBefore(sessionEndDateTime)) {
                return sessions[j]
            }
        }
        return null
    }
}