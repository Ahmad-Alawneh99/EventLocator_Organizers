package com.eventlocator.eventlocatororganizers.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eventlocator.eventlocatororganizers.databinding.ActivityIndividualSetUpProfileBinding

class IndividualSetUpProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityIndividualSetUpProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIndividualSetUpProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}