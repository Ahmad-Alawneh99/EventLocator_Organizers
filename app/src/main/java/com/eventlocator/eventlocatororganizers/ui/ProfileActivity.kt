package com.eventlocator.eventlocatororganizers.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
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

class ProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreference = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
        if (sharedPreference.getString(SharedPreferenceManager.instance.TOKEN_KEY,"") ==""){
            startActivity(Intent(this,LoginActivity::class.java))
        }
        getAndLoadOrganizerInfo()

        binding.btnCreateEvent.setOnClickListener {
            //TODO: open create event activity
        }

        binding.btnViewEvents.setOnClickListener {
            //TODO: open events activity
        }

        binding.btnEditProfile.setOnClickListener {
            val token = sharedPreference.getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")
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
                            val organizer = response.body()!!
                            binding.tvOrgName.text = organizer.name
                            binding.tvAbout.text = organizer.about
                            //TODO: Set click listener to view followers
                            binding.tvFollowers.text = organizer.numberOfFollowers.toString()
                            binding.tvEmail.text = organizer.email
                            binding.tvPhoneNumber.text = organizer.phoneNumber
                            binding.tvRating.text = organizer.rating.toString()
                            //TODO: Social media accounts
                            //TODO: Test Bitmaps
                            binding.ivOrgImage.setImageBitmap(BitmapFactory.
                            decodeStream(applicationContext.contentResolver.openInputStream(Uri.parse(organizer.image))))
                        }
                        else{
                            //TODO: Handle not found
                        }
                    }

                    override fun onFailure(call: Call<Organizer>, t: Throwable) {
                        Toast.makeText(applicationContext, t.message, Toast.LENGTH_SHORT).show()
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



}