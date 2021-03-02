package com.eventlocator.eventlocatororganizers.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eventlocator.eventlocatororganizers.databinding.ActivityEventFeedbackBinding

class EventFeedbackActivity : AppCompatActivity() {
    lateinit var binding: ActivityEventFeedbackBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}