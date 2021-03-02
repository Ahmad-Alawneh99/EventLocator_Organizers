package com.eventlocator.eventlocatororganizers.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eventlocator.eventlocatororganizers.databinding.ActivityViewParticipantsOfAnEventBinding

class ViewParticipantsOfAnEventActivity : AppCompatActivity() {
    lateinit var binding: ActivityViewParticipantsOfAnEventBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewParticipantsOfAnEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}