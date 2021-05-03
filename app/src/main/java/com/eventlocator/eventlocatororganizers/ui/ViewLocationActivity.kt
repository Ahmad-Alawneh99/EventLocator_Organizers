package com.eventlocator.eventlocatororganizers.ui

import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.commit
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.databinding.ActivityViewLocationBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class ViewLocationActivity : AppCompatActivity() {
    lateinit var binding: ActivityViewLocationBinding
    lateinit var map: GoogleMap
    lateinit var selectedLatLng: LatLng
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        selectedLatLng = intent.getParcelableExtra<LatLng>("latLng")
        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.commit {
            add(R.id.fvMap, mapFragment)
        }
        mapFragment.getMapAsync{ mp ->
            map = mp
            val locationData = Geocoder(applicationContext).getFromLocation(selectedLatLng.latitude,
                selectedLatLng.longitude, 1)
            val locationName = if (locationData.size == 0) "Unnamed location" else locationData[0].featureName
            map.moveCamera(CameraUpdateFactory.newLatLng(selectedLatLng))
            map.animateCamera(CameraUpdateFactory.zoomTo(20f))
            map.addMarker(MarkerOptions()
                .position(selectedLatLng)
                .title(locationName)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        }
    }
}