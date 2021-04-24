package com.eventlocator.eventlocatororganizers.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Base64
import com.eventlocator.eventlocatororganizers.databinding.ActivityViewImageBinding
import com.eventlocator.eventlocatororganizers.utilities.Utils
import java.io.ByteArrayInputStream

class ViewImageActivity : AppCompatActivity() {
    lateinit var binding: ActivityViewImageBinding
    lateinit var image: Bitmap
    lateinit var imageString: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        imageString = intent.getStringExtra("image")!!
        image = BitmapFactory.decodeStream(ByteArrayInputStream(Base64.decode(imageString, Base64.DEFAULT)))
        binding.ivImage.setImageBitmap(image)
    }


}