package com.eventlocator.eventlocatororganizers.retrofit

import com.eventlocator.eventlocatororganizers.data.Organizer
import retrofit2.Call
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface OrganizerService {

    @Multipart
    @POST("/organizers/signup/{type}")
    fun createOrganizer(@Part proofImage: MultipartBody.Part, @Part profilePicture: MultipartBody.Part?,
                        @Part("organizer") organizer: Organizer,
                        @Path("type") type: Int): Call<ResponseBody>

    @POST("/organizers/signup/partial")
    fun checkIfExists(@Body data: ArrayList<String>): Call<ArrayList<Int>>

    @POST("/organizers/login")
    fun login(@Body credentials: ArrayList<String>): Call<String>

    @GET("/organizers/profile")
    fun getOrganizerInfo(): Call<Organizer>

    @GET("/organizers/profile/type")
    fun getOrganizerType(): Call<Int>

    @GET("/organizers/followers")
    fun getOrganizerFollowers(): Call<ArrayList<String>>

}