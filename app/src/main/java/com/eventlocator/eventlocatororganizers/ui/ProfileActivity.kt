package com.eventlocator.eventlocatororganizers.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.get
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.data.Organizer
import com.eventlocator.eventlocatororganizers.databinding.ActivityProfileBinding
import com.eventlocator.eventlocatororganizers.retrofit.OrganizerService
import com.eventlocator.eventlocatororganizers.retrofit.RetrofitServiceFactory
import com.eventlocator.eventlocatororganizers.utilities.SharedPreferenceManager
import com.eventlocator.eventlocatororganizers.utilities.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayInputStream
import java.net.URLEncoder

class ProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityProfileBinding
    lateinit var organizer: Organizer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
        if (sharedPreferences.contains(SharedPreferenceManager.instance.FIRST_TIME_KEY)){
            startActivity(Intent(this, WelcomeActivity::class.java))
        }
        if (sharedPreferences.getString(SharedPreferenceManager.instance.TOKEN_KEY,"") ==""){
            startActivity(Intent(this,LoginActivity::class.java))
        }
        //getAndLoadOrganizerInfo()

        binding.btnCreateEvent.setOnClickListener {
            startActivity(Intent(this, CreateEventActivity::class.java))
        }

        binding.btnViewEvents.setOnClickListener {
            startActivity(Intent(this, EventsActivity::class.java))
        }

        binding.btnEditProfile.setOnClickListener {
            val token = sharedPreferences.getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")
            RetrofitServiceFactory.createServiceWithAuthentication(OrganizerService::class.java, token!!)
                    .getOrganizerType().enqueue(object: Callback<Int>{
                        override fun onResponse(call: Call<Int>, response: Response<Int>) {
                            //TODO: Check if successful
                            if (response.body()!! == 0){
                                //TODO: open organization edit profile
                            }
                            else{
                                //TODO: open individual edit profile
                            }
                        }

                        override fun onFailure(call: Call<Int>, t: Throwable) {
                        }

                    })
        }
    }


    override fun onResume() {
        super.onResume()
        getAndLoadOrganizerInfo()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_profile, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.miLogout -> {
                val sharedPreferenceEditor =
                        getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE).edit()
                sharedPreferenceEditor.putString(SharedPreferenceManager.instance.TOKEN_KEY, null)
                sharedPreferenceEditor.apply()
                //TODO: add confirmation box for logout
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }



        return super.onOptionsItemSelected(item)
    }


    private fun getAndLoadOrganizerInfo(){
        val token = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
                .getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")!!
        RetrofitServiceFactory.createServiceWithAuthentication(OrganizerService::class.java, token)
                .getOrganizerInfo().enqueue(object: Callback<Organizer> {
                    override fun onResponse(call: Call<Organizer>, response: Response<Organizer>) {
                        if (response.code()==202) {
                            organizer = response.body()!!
                            binding.tvOrgName.text = organizer.name
                            binding.tvAbout.text = organizer.about
                            //TODO: Set click listener to view followers
                            binding.tvFollowers.text = organizer.numberOfFollowers.toString()
                            binding.tvEmail.text = organizer.email
                            binding.tvPhoneNumber.text = organizer.phoneNumber
                            binding.tvRating.text = organizer.rating.toString()
                            setSocialMediaAccounts()
                            if (organizer.image!="") {
                                binding.ivOrgImage.setImageBitmap(BitmapFactory.decodeStream(
                                        ByteArrayInputStream(Base64.decode(organizer.image, Base64.DEFAULT))))
                            }
                        }
                        else if (response.code()==401){
                            Utils.instance.displayInformationalDialog(this@ProfileActivity, "Error",
                                    "401: Unauthorized access",true)
                        }
                        else if (response.code() == 404){
                            Utils.instance.displayInformationalDialog(this@ProfileActivity,
                                    "Error", "404: Organizer not found", true)
                        }
                        else if (response.code() == 500){
                            Utils.instance.displayInformationalDialog(this@ProfileActivity,
                                    "Error", "Server issue, please try again later", true)
                        }
                    }

                    override fun onFailure(call: Call<Organizer>, t: Throwable) {
                        Utils.instance.displayInformationalDialog(this@ProfileActivity,
                                "Error", "Can't connect to server", true)
                    }

                })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val a = Intent(Intent.ACTION_MAIN)
        a.addCategory(Intent.CATEGORY_HOME)
        a.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(a)
    }

    fun setSocialMediaAccounts(){
        for(i in 0 until organizer.socialMediaAccounts.size){
            if (organizer.socialMediaAccounts[i].accountName=="" && organizer.socialMediaAccounts[i].url==""){
                binding.llSocialMedia[i].visibility = View.GONE
            }
            else{
                when (i){
                    0 -> {
                        binding.llSocialMedia[i].setOnClickListener {
                            if (organizer.socialMediaAccounts[i].url!=""){
                                val intent = Intent(Intent.ACTION_VIEW,
                                Uri.parse(organizer.socialMediaAccounts[i].url))
                                startActivity(intent)
                            }
                            else if (organizer.socialMediaAccounts[i].accountName!=""){
                                val url = "https://www.facebook.com/search/top?q="+
                                        URLEncoder.encode(organizer.socialMediaAccounts[i].accountName, "UTF-8")
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                startActivity(intent)
                            }
                        }
                    }
                    1 -> {
                        binding.llSocialMedia[i].setOnClickListener {
                            if (organizer.socialMediaAccounts[i].url!=""){
                                val intent = Intent(Intent.ACTION_VIEW,
                                        Uri.parse(organizer.socialMediaAccounts[i].url))
                                startActivity(intent)
                            }
                            else if (organizer.socialMediaAccounts[i].accountName!=""){
                                val url = "https://www.youtube.com/results?search_query="+
                                        URLEncoder.encode(organizer.socialMediaAccounts[i].accountName, "UTF-8")
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                startActivity(intent)
                            }
                        }
                    }
                    2 -> {
                        binding.llSocialMedia[i].setOnClickListener {
                            if (organizer.socialMediaAccounts[i].url!=""){
                                val intent = Intent(Intent.ACTION_VIEW,
                                        Uri.parse(organizer.socialMediaAccounts[i].url))
                                startActivity(intent)
                            }
                            else if (organizer.socialMediaAccounts[i].accountName!=""){
                                val url = "https://www.instagram.com/"+
                                        organizer.socialMediaAccounts[i].accountName
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                startActivity(intent)
                            }
                        }
                    }
                    3 -> {
                        binding.llSocialMedia[i].setOnClickListener {
                            if (organizer.socialMediaAccounts[i].url!=""){
                                val intent = Intent(Intent.ACTION_VIEW,
                                        Uri.parse(organizer.socialMediaAccounts[i].url))
                                startActivity(intent)
                            }
                            else if (organizer.socialMediaAccounts[i].accountName!=""){
                                val url = "https://twitter.com/search?q="+
                                        URLEncoder.encode(organizer.socialMediaAccounts[i].accountName, "UTF-8")
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                startActivity(intent)
                            }
                        }
                    }
                    4 -> {
                        binding.llSocialMedia[i].setOnClickListener {
                            if (organizer.socialMediaAccounts[i].url!=""){
                                val intent = Intent(Intent.ACTION_VIEW,
                                        Uri.parse(organizer.socialMediaAccounts[i].url))
                                startActivity(intent)
                            }
                            else if (organizer.socialMediaAccounts[i].accountName!=""){
                                val url = "https://www.linkedin.com/search/results/all/?keywords="+
                                        URLEncoder.encode(organizer.socialMediaAccounts[i].accountName, "UTF-8")
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
        }
        if (organizer.socialMediaAccounts.size<5) binding.ivLinkedIn.visibility = View.GONE
    }

}