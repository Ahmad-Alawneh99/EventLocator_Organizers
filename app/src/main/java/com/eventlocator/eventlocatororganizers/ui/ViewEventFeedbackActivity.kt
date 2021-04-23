package com.eventlocator.eventlocatororganizers.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eventlocator.eventlocatororganizers.data.Feedback
import com.eventlocator.eventlocatororganizers.databinding.ActivityViewEventFeedbackBinding
import com.eventlocator.eventlocatororganizers.databinding.FeedbackBinding
import com.eventlocator.eventlocatororganizers.retrofit.EventService
import com.eventlocator.eventlocatororganizers.retrofit.RetrofitServiceFactory
import com.eventlocator.eventlocatororganizers.utilities.SharedPreferenceManager
import com.eventlocator.eventlocatororganizers.utilities.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewEventFeedbackActivity : AppCompatActivity() {
    lateinit var binding: ActivityViewEventFeedbackBinding
    var eventID: Long = -1
    var totalParticipants = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewEventFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getAndLoadFeedback()

    }

    private fun getAndLoadFeedback(){
        eventID = intent.getLongExtra("eventID", -1)
        totalParticipants = intent.getIntExtra("totalParticipants", -1)
        val token = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
                .getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")

        RetrofitServiceFactory.createServiceWithAuthentication(EventService::class.java, token!!)
                .getEventFeedback(eventID).enqueue(object: Callback<ArrayList<Feedback>>{
                    override fun onResponse(call: Call<ArrayList<Feedback>>, response: Response<ArrayList<Feedback>>) {
                        if (response.code() == 200){
                            val layoutManager = LinearLayoutManager(this@ViewEventFeedbackActivity,
                                    LinearLayoutManager.VERTICAL, false)
                            binding.rvEventFeedback.layoutManager = layoutManager
                            val adapter = FeedbackAdapter(response.body()!!)
                            binding.rvEventFeedback.adapter = adapter

                            binding.pbLoading.visibility = View.INVISIBLE
                        }
                        else if (response.code() == 401){
                            Utils.instance.displayInformationalDialog(this@ViewEventFeedbackActivity, "Error",
                                    "401: Unauthorized access",true)
                            binding.pbLoading.visibility = View.INVISIBLE
                        }
                        else if (response.code() == 404){
                            Utils.instance.displayInformationalDialog(this@ViewEventFeedbackActivity, "Error",
                                    "404: No participants found",true)
                            binding.pbLoading.visibility = View.INVISIBLE
                        }
                        else if (response.code() == 500){
                            Utils.instance.displayInformationalDialog(this@ViewEventFeedbackActivity,
                                    "Error", "Server issue, please try again later", true)
                            binding.pbLoading.visibility = View.INVISIBLE
                        }
                    }

                    override fun onFailure(call: Call<ArrayList<Feedback>>, t: Throwable) {
                        Utils.instance.displayInformationalDialog(this@ViewEventFeedbackActivity,
                                "Error", "Can't connect to server", true)
                        binding.pbLoading.visibility = View.INVISIBLE
                    }

                })
    }
}

class FeedbackAdapter(private val feedback: ArrayList<Feedback>): RecyclerView.Adapter<FeedbackAdapter.FeedbackHolder>(){

    inner class FeedbackHolder(var binding: FeedbackBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackHolder {
        val binding = FeedbackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedbackHolder(binding)
    }

    override fun onBindViewHolder(holder: FeedbackHolder, position: Int) {
        holder.binding.tvRating.text = feedback[position].rating.toString() + "/5"
        holder.binding.tvFeedback.text = feedback[position].feedback
    }

    override fun getItemCount(): Int = feedback.size
}