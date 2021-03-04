package com.eventlocator.eventlocatororganizers.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eventlocator.eventlocatororganizers.databinding.ActivityCheckInParticipantBinding

class CheckInParticipantActivity : AppCompatActivity() {
    lateinit var binding: ActivityCheckInParticipantBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckInParticipantBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}