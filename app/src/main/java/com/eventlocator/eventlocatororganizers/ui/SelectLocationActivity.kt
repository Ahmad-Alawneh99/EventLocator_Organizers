package com.eventlocator.eventlocatororganizers.ui

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.commit
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.databinding.ActivitySelectLocationBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class SelectLocationActivity : AppCompatActivity() {
    lateinit var binding: ActivitySelectLocationBinding
    lateinit var map: GoogleMap
    lateinit var selectedLatLng: LatLng
    lateinit var locationName: String
    lateinit var marker: Marker
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnConfirm.isEnabled = false
        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.commit {
            add(R.id.fvMap, mapFragment)
        }
        mapFragment.getMapAsync{ mp ->
            map = mp
            val jordanBounds = LatLngBounds(LatLng(29.52667,35.00778), LatLng(32.69833, 36.83113))
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(jordanBounds, 0))

            map.setOnMapLongClickListener {
                if (this::marker.isInitialized) marker.remove()
                selectedLatLng = it
                val location = Geocoder(applicationContext).getFromLocation(selectedLatLng.latitude,
                        selectedLatLng.longitude, 1)[0].getAddressLine(0)
                locationName = if (location==null) "Unnamed location" else location
                marker = map.addMarker(MarkerOptions()
                        .position(selectedLatLng)
                        .title(locationName)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))

                binding.btnConfirm.isEnabled = true

            }
        }

        binding.btnConfirm.setOnClickListener {
            val intent = Intent()
            intent.putExtra("latlng", selectedLatLng)
            intent.putExtra("name", locationName)

            setResult(Activity.RESULT_OK, intent)
            finish()

        }
    }
}