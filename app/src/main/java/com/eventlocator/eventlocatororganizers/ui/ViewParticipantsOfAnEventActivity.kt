package com.eventlocator.eventlocatororganizers.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eventlocator.eventlocatororganizers.data.Participant
import com.eventlocator.eventlocatororganizers.databinding.ActivityViewParticipantsOfAnEventBinding
import com.eventlocator.eventlocatororganizers.databinding.ParticipantInEventBinding
import com.eventlocator.eventlocatororganizers.retrofit.EventService
import com.eventlocator.eventlocatororganizers.retrofit.RetrofitServiceFactory
import com.eventlocator.eventlocatororganizers.utilities.SharedPreferenceManager
import com.eventlocator.eventlocatororganizers.utilities.Utils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigDecimal

class ViewParticipantsOfAnEventActivity : AppCompatActivity() {
    lateinit var binding: ActivityViewParticipantsOfAnEventBinding
    var eventID: Long = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewParticipantsOfAnEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
        eventID = intent.getLongExtra("eventID", -1)
        getAndLoadParticipants()

    }

    private fun getAndLoadParticipants(){
        val token = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
                .getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")
        RetrofitServiceFactory.createServiceWithAuthentication(EventService::class.java, token!!)
                .getParticipantsOfAnEvent(eventID).enqueue(object : Callback<ArrayList<Participant>> {
                    override fun onResponse(call: Call<ArrayList<Participant>>, response: Response<ArrayList<Participant>>) {
                        if (response.code() == 200) {
                            val layoutManager = LinearLayoutManager(this@ViewParticipantsOfAnEventActivity,
                                    LinearLayoutManager.VERTICAL,false)
                            val participants  = response.body()!!
                            binding.rvParticipants.layoutManager = layoutManager
                            val adapter = ParticipantInEventAdapter(participants)
                            binding.rvParticipants.adapter = adapter
                            binding.pbLoading.visibility = View.INVISIBLE
                            binding.tvNumberOfParticipant.text = participants.size.toString()
                            //Size here is always greater than 0
                            var sum: Double = 0.0
                            for(i in 0 until participants.size){
                                sum+= participants[i].rating
                            }
                            binding.tvAvgParticipantRating.text = BigDecimal(sum/participants.size).setScale(2).toString() + "/5"

                        }
                        else if (response.code()==401){
                            Utils.instance.displayInformationalDialog(this@ViewParticipantsOfAnEventActivity, "Error",
                                    "401: Unauthorized access",true)

                            binding.pbLoading.visibility = View.INVISIBLE
                        }
                        else if (response.code() == 404){
                            Utils.instance.displayInformationalDialog(this@ViewParticipantsOfAnEventActivity,
                                    "Error", "No participants found", true)

                            binding.pbLoading.visibility = View.INVISIBLE
                        }
                        else if (response.code() == 500) {
                            Utils.instance.displayInformationalDialog(this@ViewParticipantsOfAnEventActivity,
                                    "Error", "Server issue, please try again later", true)
                            binding.pbLoading.visibility = View.INVISIBLE
                        }
                    }
                    override fun onFailure(call: Call<ArrayList<Participant>>, t: Throwable) {
                        Utils.instance.displayInformationalDialog(this@ViewParticipantsOfAnEventActivity,
                                "Error", "Can't connect to server", true)
                        binding.pbLoading.visibility = View.INVISIBLE
                    }

                })
    }
}

class ParticipantInEventAdapter(private val participants: ArrayList<Participant>):
        RecyclerView.Adapter<ParticipantInEventAdapter.ParticipantInEventViewHolder>(){

    inner class ParticipantInEventViewHolder(var binding: ParticipantInEventBinding):
            RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantInEventViewHolder {
        val binding = ParticipantInEventBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return ParticipantInEventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParticipantInEventViewHolder, position: Int) {
        holder.binding.tvParticipantName.text = participants[position].firstName + " " +participants[position].lastName
        holder.binding.tvParticipantRating.text = participants[position].rating.toString() +"/5"
    }

    override fun getItemCount(): Int = participants.size
}