package com.eventlocator.eventlocatororganizers.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eventlocator.eventlocatororganizers.databinding.ActivityEditConfirmedEventBinding

class EditConfirmedEventActivity : AppCompatActivity() {
    lateinit var binding: ActivityEditConfirmedEventBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditConfirmedEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}