package com.eventlocator.eventlocatororganizers.ui

import android.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import com.eventlocator.eventlocatororganizers.data.Participant
import com.eventlocator.eventlocatororganizers.databinding.ActivityViewParticipantsDuringALimitedLocatedSessionBinding
import com.eventlocator.eventlocatororganizers.retrofit.EventService
import com.eventlocator.eventlocatororganizers.retrofit.RetrofitServiceFactory
import com.eventlocator.eventlocatororganizers.utilities.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewParticipantsDuringALimitedLocatedSession : AppCompatActivity() {
    lateinit var binding: ActivityViewParticipantsDuringALimitedLocatedSessionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewParticipantsDuringALimitedLocatedSessionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val eventID = intent.getIntExtra("eventID", -1)
        val token = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
            .getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")
        RetrofitServiceFactory.createServiceWithAuthentication(EventService::class.java, token!!)
            .getParticipantsOfALimitedEvent(eventID).enqueue(object : Callback<ArrayList<Participant>> {
                override fun onResponse(call: Call<ArrayList<Participant>>, response: Response<ArrayList<Participant>>) {
                    //TODO: Check http code
                    val adapter = ArrayAdapter(applicationContext, R.layout.simple_expandable_list_item_1, response.body()!!)
                    binding.lvParticipants.adapter = adapter

                }

                override fun onFailure(call: Call<ArrayList<Participant>>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_SHORT).show()
                }

            })
    }
}