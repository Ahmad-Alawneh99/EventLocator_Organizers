package com.eventlocator.eventlocatororganizers.data

import com.eventlocator.eventlocatororganizers.utilities.EventCategory
import com.eventlocator.eventlocatororganizers.utilities.EventStatus
import java.time.LocalDate
import java.time.LocalDateTime

class Event(var id: Long, var name: String, var description: String, var categories: List<EventCategory>, var startDate: String,
            var endDate: String, var registrationCloseDateTime: String, var status: EventStatus, var maxParticipants: Int,
            var rating: Double, var sessions: List<Session>, var participants: List<Participant>, var feedback: List<Feedback>,
            var locatedEventData: LocatedEventData?, var canceledEventData: CanceledEventData?, var image: String, var whatsAppLink: String) {

    //called only when creating new events.
    private constructor(eventBuilder: EventBuilder) : this(-1, eventBuilder.name, eventBuilder.description, eventBuilder.categories,
                        eventBuilder.startDate, eventBuilder.endDate, eventBuilder.registrationCloseDateTime, EventStatus.PENDING,
                        eventBuilder.maxParticipants, -1.0, eventBuilder.sessions, ArrayList<Participant>(),
                        ArrayList<Feedback>(), eventBuilder.locatedEventData, null, "", eventBuilder.whatsAppLink)


    class EventBuilder(var name: String, var description: String, var categories: List<EventCategory>,
                       var startDate: String, var endDate: String, var sessions: List<Session>,
                       var registrationCloseDateTime: String, ){
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
}