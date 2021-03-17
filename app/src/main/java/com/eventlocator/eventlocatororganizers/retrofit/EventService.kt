package com.eventlocator.eventlocatororganizers.retrofit

import com.eventlocator.eventlocatororganizers.data.Event
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface EventService {

    @Multipart
    @POST("/organizer/create/event")
    fun createEvent(@Part eventImage: MultipartBody.Part, @Part("event") event: Event): Call<ResponseBody> //change response

    @GET("/organizer/events")
    fun getEvents(): Call<ArrayList<Event>>

}