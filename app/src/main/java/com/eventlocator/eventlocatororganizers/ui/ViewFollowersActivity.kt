package com.eventlocator.eventlocatororganizers.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eventlocator.eventlocatororganizers.databinding.ActivityViewFollowersBinding
import com.eventlocator.eventlocatororganizers.databinding.ParticipantAsFollowerBinding
import com.eventlocator.eventlocatororganizers.retrofit.OrganizerService
import com.eventlocator.eventlocatororganizers.retrofit.RetrofitServiceFactory
import com.eventlocator.eventlocatororganizers.utilities.SharedPreferenceManager
import com.eventlocator.eventlocatororganizers.utilities.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewFollowersActivity : AppCompatActivity() {
    lateinit var binding: ActivityViewFollowersBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewFollowersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getAndLoadParticipants()

    }

    private fun getAndLoadParticipants(){
        val token = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
                .getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")
        RetrofitServiceFactory.createServiceWithAuthentication(OrganizerService::class.java, token!!)
                .getOrganizerFollowers().enqueue(object: Callback<ArrayList<String>>{
                    override fun onResponse(call: Call<ArrayList<String>>, response: Response<ArrayList<String>>) {
                        if (response.code()==202){
                            val layoutManager = LinearLayoutManager(this@ViewFollowersActivity, LinearLayoutManager.VERTICAL, false)
                            binding.rvFollowers.layoutManager = layoutManager
                            val adapter = ParticipantAsFollowerAdapter(response.body()!!)
                            binding.rvFollowers.adapter = adapter
                            binding.pbLoading.visibility = View.INVISIBLE
                        }
                        else if (response.code() == 401){
                            Utils.instance.displayInformationalDialog(this@ViewFollowersActivity, "Error",
                                    "401: Unauthorized access",true)
                            binding.pbLoading.visibility = View.INVISIBLE
                        }
                        else if (response.code() == 404){
                            Utils.instance.displayInformationalDialog(this@ViewFollowersActivity, "Error",
                                    "404: No followers found",true)
                            binding.pbLoading.visibility = View.INVISIBLE
                        }
                        else if (response.code() == 500){
                            Utils.instance.displayInformationalDialog(this@ViewFollowersActivity,
                                    "Error", "Server issue, please try again later", true)
                            binding.pbLoading.visibility = View.INVISIBLE
                        }
                    }

                    override fun onFailure(call: Call<ArrayList<String>>, t: Throwable) {
                        Utils.instance.displayInformationalDialog(this@ViewFollowersActivity,
                                "Error", "Can't connect to server", true)
                        binding.pbLoading.visibility = View.INVISIBLE
                    }

                })
    }
}

class ParticipantAsFollowerAdapter(private val participants: ArrayList<String>):
        RecyclerView.Adapter<ParticipantAsFollowerAdapter.ParticipantAsFollowerViewHolder>(){

    inner class ParticipantAsFollowerViewHolder(var binding: ParticipantAsFollowerBinding):
            RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantAsFollowerViewHolder {
        val binding = ParticipantAsFollowerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParticipantAsFollowerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParticipantAsFollowerViewHolder, position: Int) {
        holder.binding.tvParticipantName.text = participants[position]
    }

    override fun getItemCount(): Int = participants.size


}