package com.eventlocator.eventlocatororganizers.retrofit

import com.eventlocator.eventlocatororganizers.data.CanceledEventData
import com.eventlocator.eventlocatororganizers.data.Event
import com.eventlocator.eventlocatororganizers.data.Participant
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface EventService {

    @Multipart
    @POST("/organizers/events/create")
    fun createEvent(@Part eventImage: MultipartBody.Part, @Part("event") event: Event): Call<ResponseBody> //change response

    @GET("/organizers/events")
    fun getEvents(): Call<ArrayList<Event>>

    @GET("/organizers/events/{id}")
    fun getEvent(@Path("id") eventID: Int): Call<Event>

    @GET("/organizers/events/{id}/participants")
    fun getParticipantsOfAnEvent(@Path("id") eventID: Int): Call<ArrayList<Participant>>

    @POST("/organizers/events/{id}/cancel")
    fun cancelEvent(@Path("id") eventID: Int, @Body cancelledEventData: CanceledEventData): Call<ResponseBody>

    @GET("/organizers/events/limited/{id}/participants")
    fun getParticipantsOfALimitedEvent(@Path("id") eventID: Int): Call<ArrayList<Participant>>

}