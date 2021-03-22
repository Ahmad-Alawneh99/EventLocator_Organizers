package com.eventlocator.eventlocatororganizers.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import com.eventlocator.eventlocatororganizers.data.Participant
import com.eventlocator.eventlocatororganizers.databinding.ActivityViewParticipantsOfAnEventBinding
import com.eventlocator.eventlocatororganizers.retrofit.EventService
import com.eventlocator.eventlocatororganizers.retrofit.RetrofitServiceFactory
import com.eventlocator.eventlocatororganizers.utilities.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewParticipantsOfAnEventActivity : AppCompatActivity() {
    lateinit var binding: ActivityViewParticipantsOfAnEventBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewParticipantsOfAnEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val eventID = intent.getIntExtra("eventID", -1)
        val token = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
            .getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")
        RetrofitServiceFactory.createServiceWithAuthentication(EventService::class.java, token!!)
            .getParticipantsOfAnEvent(eventID).enqueue(object : Callback<ArrayList<Participant>> {
                override fun onResponse(call: Call<ArrayList<Participant>>, response: Response<ArrayList<Participant>>) {
                    //TODO: Check http code
                    val adapter = ArrayAdapter(applicationContext, android.R.layout.simple_expandable_list_item_1, response.body()!!)
                    binding.lvParticipants.adapter = adapter

                }

                override fun onFailure(call: Call<ArrayList<Participant>>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_SHORT).show()
                }

            })
    }
}