package com.eventlocator.eventlocatororganizers.retrofit

import com.eventlocator.eventlocatororganizers.data.CanceledEventData
import com.eventlocator.eventlocatororganizers.data.Event
import com.eventlocator.eventlocatororganizers.data.Feedback
import com.eventlocator.eventlocatororganizers.data.Participant
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface EventService {

    @Multipart
    @POST("/organizers/events/create")
    fun createEvent(@Part eventImage: MultipartBody.Part, @Part("event") event: Event): Call<String>

    @GET("/organizers/events")
    fun getEvents(): Call<ArrayList<Event>>

    @GET("/organizers/events/{id}")
    fun getEvent(@Path("id") eventID: Long): Call<Event>

    @GET("/organizers/events/{id}/participants")
    fun getParticipantsOfAnEvent(@Path("id") eventID: Long): Call<ArrayList<Participant>>

    @POST("/organizers/events/{id}/cancel/{late}")
    fun cancelEvent(@Path("id") eventID: Long, @Body cancelledEventData: CanceledEventData, @Path("late") late: Boolean): Call<ResponseBody>

    @GET("/organizers/events/limited/{id}/participants")
    fun getParticipantsOfALimitedEvent(@Path("id") eventID: Long): Call<ArrayList<Participant>>

    @GET("/organizers/events/{eventID}/session/{sessionID}/participant/{participantID}")
    fun prepareToCheckInParticipant(@Path("eventID") eventID: Long, @Path("sessionID") sessionID: Int,
                                                 @Path("participantID") participantID: Long): Call<ArrayList<String>>

    @GET("/organizers/events/{eventID}/session/{sessionID}/participant/{participantID}/confirm")
    fun checkInParticipant(@Path("eventID") eventID: Long, @Path("sessionID") sessionID: Int,
                                                 @Path("participantID") participantID: Long): Call<ResponseBody>

    @GET("/organizers/events/{id}/attendanceStatistics")
    fun getAttendanceStatisticsForAnEvent(@Path("id") eventID: Long): Call<JsonObject>

    @GET("/organizers/events/{id}/feedback")
    fun getEventFeedback(@Path("id") eventID: Long): Call<ArrayList<Feedback>>

    @POST("/organizers/events/{id}/emailParticipants")
    fun emailParticipantsOfAnEvent(@Path("id")eventID: Long, @Body email: ArrayList<String>): Call<ResponseBody>

    @Multipart
    @PATCH("/organizers/events/{id}/editPending")
    fun editPendingEvent(@Path("id") eventID: Long, @Part("event") event:Event, @Part image: MultipartBody.Part?): Call<String>


    @PATCH("/organizers/events/{id}/editConfirmed")
    fun editConfirmedEvent(@Path("id") eventID: Long, @Body event: Event): Call<ResponseBody>
}