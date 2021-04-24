package com.eventlocator.eventlocatororganizers.ui

import android.R
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eventlocator.eventlocatororganizers.data.Participant
import com.eventlocator.eventlocatororganizers.databinding.ActivityViewParticipantsDuringALimitedLocatedSessionBinding
import com.eventlocator.eventlocatororganizers.databinding.ParticipantInEventBinding
import com.eventlocator.eventlocatororganizers.databinding.ParticipantInLimitedEventBinding
import com.eventlocator.eventlocatororganizers.retrofit.EventService
import com.eventlocator.eventlocatororganizers.retrofit.RetrofitServiceFactory
import com.eventlocator.eventlocatororganizers.utilities.SharedPreferenceManager
import com.eventlocator.eventlocatororganizers.utilities.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigDecimal

class ViewParticipantsDuringALimitedLocatedSession : AppCompatActivity() {
    lateinit var binding: ActivityViewParticipantsDuringALimitedLocatedSessionBinding
    var eventID: Long = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewParticipantsDuringALimitedLocatedSessionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        eventID = intent.getLongExtra("eventID", -1)
        getAndLoadParticipants()


    }

    private fun getAndLoadParticipants(){
        val token = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
                .getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")
        RetrofitServiceFactory.createServiceWithAuthentication(EventService::class.java, token!!)
                .getParticipantsOfALimitedEvent(eventID).enqueue(object : Callback<ArrayList<Participant>> {
                    override fun onResponse(call: Call<ArrayList<Participant>>, response: Response<ArrayList<Participant>>) {
                        if (response.code() == 200){
                            val participants = response.body()!!
                            val layoutManager = LinearLayoutManager(this@ViewParticipantsDuringALimitedLocatedSession,
                                    LinearLayoutManager.VERTICAL, false)
                            binding.rvParticipants.layoutManager = layoutManager
                            val adapter = ParticipantInLimitedEventAdapter(participants)
                            binding.rvParticipants.adapter = adapter
                            binding.tvNumberOfParticipant.text = participants.size.toString()

                            var checkedIn = 0
                            var notCheckedIn = 0
                            var sum = 0.0
                            for(i in 0 until participants.size){
                                if (participants[i].arrivalTime==""){
                                    notCheckedIn ++
                                }
                                else checkedIn++
                                sum+=participants[i].rating
                            }

                            binding.tvCheckedInParticipants.text = checkedIn.toString()
                            binding.tvNotCheckedInParticipants.text = notCheckedIn.toString()
                            binding.tvAvgParticipantRating.text = BigDecimal(sum/participants.size).setScale(2).toString() + "/5"


                        }
                        else if (response.code() == 401){
                            Utils.instance.displayInformationalDialog(this@ViewParticipantsDuringALimitedLocatedSession,
                                    "Error", "401: Unauthorized access",true)
                        }
                        else if (response.code() == 404){
                            Utils.instance.displayInformationalDialog(this@ViewParticipantsDuringALimitedLocatedSession,
                                    "Error", "404: No participants found",true)
                        }
                        else if (response.code() == 500){
                            Utils.instance.displayInformationalDialog(this@ViewParticipantsDuringALimitedLocatedSession,
                                    "Error", "Server issue, please try again later", true)
                        }

                        binding.pbLoading.visibility = View.INVISIBLE

                    }

                    override fun onFailure(call: Call<ArrayList<Participant>>, t: Throwable) {
                        Utils.instance.displayInformationalDialog(this@ViewParticipantsDuringALimitedLocatedSession,
                                "Error", "Can't connect to server", true)
                        binding.pbLoading.visibility = View.INVISIBLE
                    }

                })
    }
}

class ParticipantInLimitedEventAdapter(private val participants: ArrayList<Participant>):
        RecyclerView.Adapter<ParticipantInLimitedEventAdapter.ParticipantInLimitedEventViewHolder>(){
    lateinit var context: Context
    inner class ParticipantInLimitedEventViewHolder(var binding: ParticipantInLimitedEventBinding):
            RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantInLimitedEventViewHolder {
        val binding = ParticipantInLimitedEventBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        context = parent.context
        return ParticipantInLimitedEventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParticipantInLimitedEventViewHolder, position: Int) {
        holder.binding.tvParticipantName.text = participants[position].firstName + " " +participants[position].lastName
        holder.binding.tvParticipantRating.text = participants[position].rating.toString() +"/5"
        holder.binding.tvHasCheckedIn.text = if(participants[position].arrivalTime!="") "YES" else "NO"
        if (participants[position].arrivalTime!=""){
            holder.binding.tvHasCheckedIn.setTextColor(ContextCompat.getColor(context,
                    com.eventlocator.eventlocatororganizers.R.color.green))
        }
        else{
            holder.binding.tvHasCheckedIn.setTextColor(ContextCompat.getColor(context,
                    com.eventlocator.eventlocatororganizers.R.color.design_default_color_error))
        }
    }

    override fun getItemCount(): Int = participants.size
}