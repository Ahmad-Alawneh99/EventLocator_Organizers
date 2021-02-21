package com.eventlocator.eventlocatororganizers.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eventlocator.eventlocatororganizers.databinding.ActivityOrganizationSetUpProfileBinding

class OrganizationSetUpProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityOrganizationSetUpProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrganizationSetUpProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}