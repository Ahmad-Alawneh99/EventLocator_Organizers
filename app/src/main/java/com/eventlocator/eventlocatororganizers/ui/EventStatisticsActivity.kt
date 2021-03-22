package com.eventlocator.eventlocatororganizers.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eventlocator.eventlocatororganizers.databinding.ActivityEventStatisticsBinding

class EventStatisticsActivity : AppCompatActivity() {
    lateinit var binding: ActivityEventStatisticsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}