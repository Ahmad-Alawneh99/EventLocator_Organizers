package com.eventlocator.eventlocatororganizers.data

import com.eventlocator.eventlocatororganizers.utilities.EventCategory
import java.time.LocalDate
import java.time.LocalDateTime

//TODO: add event image
class Event(var id: Long, var name: String, var description: String, var categories: List<EventCategory>, var startDate: LocalDate,
            var endDate: LocalDate, var registrationCloseDateTime: LocalDateTime, var status: String, var maxParticipants: Int,
            var rating: Double, var sessions: List<Session>, var participants: List<Participant>, var feedback: List<Feedback>,
            var locatedEventData: LocatedEventData, var canceledEventData: CanceledEventData?) {

    //called only when creating new events.
    private constructor(eventBuilder: EventBuilder) : this(-1, eventBuilder.name, eventBuilder.description, eventBuilder.categories,
                        eventBuilder.startDate, eventBuilder.endDate, eventBuilder.registrationCloseDateTime, "Pending",
                        eventBuilder.maxParticipants, -1.0, eventBuilder.sessions, ArrayList<Participant>(),
                        ArrayList<Feedback>(), eventBuilder.locatedEventData, null)


    class EventBuilder(var name: String, var description: String, var categories: List<EventCategory>,
                       var startDate: LocalDate, var endDate: LocalDate, var sessions: List<Session>){
        var maxParticipants: Int = -1
        lateinit var registrationCloseDateTime: LocalDateTime
        lateinit var locatedEventData: LocatedEventData

        fun setMaxParticipants(value: Int): EventBuilder{
            this.maxParticipants = value
            return this
        }

        fun setRegistrationCloseDateTime(dateTime: LocalDateTime): EventBuilder{
            this.registrationCloseDateTime = dateTime
            return this
        }

        fun setLocatedEventData(locatedEventData: LocatedEventData): EventBuilder{
            this.locatedEventData = locatedEventData
            return this
        }

        fun build(): Event{
            return Event(this)
        }
    }
}