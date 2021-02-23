package com.eventlocator.eventlocatororganizers.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eventlocator.eventlocatororganizers.databinding.ActivityIndividualEditProfileBinding

class IndividualEditProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityIndividualEditProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIndividualEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}