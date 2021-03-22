package com.eventlocator.eventlocatororganizers.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eventlocator.eventlocatororganizers.databinding.ActivityViewEventFeedbackBinding
import com.eventlocator.eventlocatororganizers.utilities.SharedPreferenceManager

class ViewEventFeedbackActivity : AppCompatActivity() {
    lateinit var binding: ActivityViewEventFeedbackBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewEventFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val eventID = intent.getIntExtra("eventID", -1)
        val token = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
            .getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")
        //TODO: add call to get event feedback
    }
}