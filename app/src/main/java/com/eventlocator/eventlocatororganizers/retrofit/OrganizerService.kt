package com.eventlocator.eventlocatororganizers.retrofit

import com.eventlocator.eventlocatororganizers.data.Organizer
import retrofit2.Call
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface OrganizerService {

    @Multipart
    @POST("/createOrganizer")
    fun createOrganizer(@Part proofImage: MultipartBody.Part, @Part profilePicture: MultipartBody.Part?,
                        @Part("organizer") organizer: Organizer): Call<ResponseBody>

}