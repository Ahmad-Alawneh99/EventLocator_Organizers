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
import com.eventlocator.eventlocatororganizers.utilities.Utils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewParticipantsOfAnEventActivity : AppCompatActivity() {
    lateinit var binding: ActivityViewParticipantsOfAnEventBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewParticipantsOfAnEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val eventID = intent.getLongExtra("eventID", -1)
        val token = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
            .getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")
        RetrofitServiceFactory.createServiceWithAuthentication(EventService::class.java, token!!)
            .getParticipantsOfAnEvent(eventID).enqueue(object : Callback<ArrayList<Participant>> {
                override fun onResponse(call: Call<ArrayList<Participant>>, response: Response<ArrayList<Participant>>) {
                    if (response.code() == 200) {
                        val adapter = ArrayAdapter(this@ViewParticipantsOfAnEventActivity,
                                android.R.layout.simple_expandable_list_item_1, response.body()!!)
                        binding.lvParticipants.adapter = adapter
                    }
                    else if (response.code()==401){
                        Utils.instance.displayInformationalDialog(this@ViewParticipantsOfAnEventActivity, "Error",
                                "401: Unauthorized access",true)
                    }
                    else if (response.code() == 404){
                        Utils.instance.displayInformationalDialog(this@ViewParticipantsOfAnEventActivity,
                                "Error", "No participants found", false)
                    }
                    else if (response.code() == 500) {
                        Utils.instance.displayInformationalDialog(this@ViewParticipantsOfAnEventActivity,
                                "Error", "Server issue, please try again later", false)
                    }
                }
                    override fun onFailure(call: Call<ArrayList<Participant>>, t: Throwable) {
                        Utils.instance.displayInformationalDialog(this@ViewParticipantsOfAnEventActivity,
                                "Error", "Can't connect to server", false)
                    }

            })
    }
}