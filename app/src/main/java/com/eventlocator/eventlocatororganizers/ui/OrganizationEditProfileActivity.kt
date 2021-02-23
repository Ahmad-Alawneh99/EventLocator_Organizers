package com.eventlocator.eventlocatororganizers.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eventlocator.eventlocatororganizers.databinding.ActivityOrganizationEditProfileBinding


class OrganizationUpdateProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityOrganizationEditProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrganizationEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}