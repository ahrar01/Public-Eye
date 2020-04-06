package com.qdesigns.publiceye.ui.post_complaint

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.qdesigns.publiceye.R
import com.qdesigns.publiceye.utils.GpsUtils
import com.rommansabbir.locationlistenerandroid.LocationCallback
import com.rommansabbir.locationlistenerandroid.LocationListener
import com.rommansabbir.locationlistenerandroid.PermissionCallback
import kotlinx.android.synthetic.main.activity_get_location.*
import java.io.File

class GetLocation : AppCompatActivity(), PermissionCallback {
    lateinit var imageFile: File
    var vehicleNumber = ""
    private lateinit var gpsUtils: GpsUtils
    var latitutde: Double? = null
    var longitude: Double? = null
    var currentAddress: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_location)
        imageFile = intent.extras?.get("imageFile") as File
        vehicleNumber = intent.extras?.get("vehicle_number") as String

        gpsUtils = GpsUtils(this)
        /**
         * Set LocationListener component with activity
         */
        LocationListener.setComponent(this)
        setupClickListeners()

    }

    private fun setupClickListeners() {
        address_edit_input_get_locationLayout.setEndIconOnClickListener {
            gpsUtils.getLatLong { lat, long ->
                println("location is $lat + $long")
            }
            LocationListener.getLocation(object : LocationCallback {
                override fun onLocationSuccess(location: Location) {

                    latitutde = location.latitude
                    longitude = location.longitude

                    currentAddress = gpsUtils.getAddress(
                        this@GetLocation,
                        location.latitude,
                        location.longitude
                    )!!
                    address_name_get_edit_text.setText(currentAddress)
                }
            })
        }
    }


    override fun onPermissionRequest(isGranted: Boolean) {
        /**
         * Check if granted or not
         */
        if (isGranted) {
            /**
             * Get location for a single time
             */
            gpsUtils.getLatLong { lat, long ->
                println("location is $lat + $long")
            }
            LocationListener.getLocation(object : LocationCallback {
                override fun onLocationSuccess(location: Location) {

                    currentAddress = gpsUtils.getAddress(
                        this@GetLocation,
                        location.latitude,
                        location.longitude
                    )!!

                }
            })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        /**
         * Pass to the LocationListener
         */
        LocationListener.processResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        /**
         * Self Destroy LocationListener
         */
        LocationListener.selfDestroy()
    }

}
