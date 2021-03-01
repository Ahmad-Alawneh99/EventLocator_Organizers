package com.eventlocator.eventlocatororganizers.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eventlocator.eventlocatororganizers.databinding.ActivityViewEventBinding

class ViewEventActivity : AppCompatActivity() {
    lateinit var binding: ActivityViewEventBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}