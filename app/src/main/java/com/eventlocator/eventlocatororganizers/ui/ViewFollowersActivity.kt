package com.eventlocator.eventlocatororganizers.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eventlocator.eventlocatororganizers.databinding.ActivityViewFollowersBinding

class ViewFollowersActivity : AppCompatActivity() {
    lateinit var binding: ActivityViewFollowersBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewFollowersBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}