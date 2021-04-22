package com.eventlocator.eventlocatororganizers.retrofit

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitServiceFactory {

    companion object{
        private val httpClient = OkHttpClient.Builder()
        private val gson = GsonBuilder().setLenient().create()
        private val retrofitBuilder: Retrofit.Builder = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient.build())

        private var retrofit = retrofitBuilder.build()

        fun <S> createService(serviceClass: Class<S>): S = retrofit.create(serviceClass)

        fun <S> createServiceWithAuthentication(serviceClass: Class<S>, token: String): S{
            httpClient.interceptors().clear()
            httpClient.addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                        .header("Authorization", token)
                        .build()
                chain.proceed(request)
            }


            retrofitBuilder.client(httpClient.build()) //rebuild with the new header
            retrofit = retrofitBuilder.build()

            return retrofit.create(serviceClass)


        }
    }


}