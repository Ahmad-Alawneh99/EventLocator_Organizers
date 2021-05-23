package com.eventlocator.eventlocatororganizers.ui

import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eventlocator.eventlocatororganizers.databinding.ActivityCreateEventHelpBinding

class CreateEventHelpActivity : AppCompatActivity() {
    lateinit var binding: ActivityCreateEventHelpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEventHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}