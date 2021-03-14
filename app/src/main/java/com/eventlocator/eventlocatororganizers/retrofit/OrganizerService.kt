package com.eventlocator.eventlocatororganizers.retrofit

import com.eventlocator.eventlocatororganizers.data.Organizer
import retrofit2.Call
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface OrganizerService {

    @Multipart
    @POST("/createOrganizer")
    fun createOrganizer(@Part proofImage: MultipartBody.Part, @Part profilePicture: MultipartBody.Part?,
                        @Part("organizer") organizer: Organizer): Call<ResponseBody>

    @POST("/organizerLogin")
    fun login(@Body credentials: ArrayList<String>): Call<String>

    @GET("/organizerInfo")
    fun getOrganizerInfo(): Call<Organizer>

}