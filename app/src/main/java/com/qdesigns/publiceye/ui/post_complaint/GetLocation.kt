package com.qdesigns.publiceye.ui.post_complaint

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import com.qdesigns.publiceye.R
import com.qdesigns.publiceye.database.modal.Complaints
import com.qdesigns.publiceye.ui.home.MainActivity
import com.qdesigns.publiceye.utils.GpsUtils
import com.qdesigns.publiceye.utils.setProgressDialog
import com.qdesigns.publiceye.viewmodel.FirestoreViewModel
import com.rommansabbir.locationlistenerandroid.LocationCallback
import com.rommansabbir.locationlistenerandroid.LocationListener
import com.rommansabbir.locationlistenerandroid.PermissionCallback
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_get_location.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.util.*

class GetLocation : AppCompatActivity(), PermissionCallback {
    private val TAG = GetLocation::class.java.simpleName

    lateinit var imageFile: File
    var vehicleNumber = ""
    private lateinit var gpsUtils: GpsUtils
    var latitutde: Double? = null
    var longitude: Double? = null
    var currentAddress: String = ""
    var user = FirebaseAuth.getInstance().currentUser!!
    var anonymousName = ""
    var firestoreViewModel: FirestoreViewModel? = null
    val firestoreDB: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    var numberOfPost: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_location)
        imageFile = intent.extras?.get("imageFile") as File
        vehicleNumber = intent.extras?.get("vehicle_number") as String
        anonymousName = intent.extras?.get("anonymousName") as String
        gpsUtils = GpsUtils(this)
        firestoreViewModel = ViewModelProvider(this).get(FirestoreViewModel::class.java)

        /**
         * Set LocationListener component with activity
         */
        LocationListener.setComponent(this)
        setupClickListeners()
        getNumberOfPost()
    }

    private fun getNumberOfPost() {
        val docRef = firestoreDB.collection("users").document(user.uid)

        // Get the document, forcing the SDK to use the offline cache
        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Document found in the offline cache
                val document = task.result
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    numberOfPost = document?.data?.getOrDefault("numberOfPost", 0.toLong()) as Long
                }

                Log.d(TAG, "Cached document numberOfPost data:$numberOfPost ")
            } else {
                Log.d(TAG, "Cached get failed: ", task.exception)
            }
        }
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

        post_complaint_button.setOnClickListener {
            val address_name_get = address_name_get_edit_text.text.toString().trim()

            if (isFormValid()) {
                uploadImageAndSaveUri(address_name_get, vehicleNumber, imageFile)
            }

        }
    }

    private fun uploadImageAndSaveUri(
        addressNameGet: String,
        vehicleNumber: String,
        imageFile: File
    ) {
        val dialog = setProgressDialog(this)
        dialog.show()
        val baos = ByteArrayOutputStream()
        val storageRef = FirebaseStorage.getInstance()
            .reference
            .child("ComplaintsPics/${FirebaseAuth.getInstance().currentUser?.uid}/${Timestamp(Date()).toString()}")

        val stream = FileInputStream(File(imageFile.toURI()))


        val upload = storageRef.putStream(stream)

        upload.addOnCompleteListener { uploadTask ->
            if (uploadTask.isSuccessful) {
                storageRef.downloadUrl.addOnCompleteListener { urlTask ->
                    var contact = ""
                    if (user.email.isNullOrEmpty()) {
                        contact = user.phoneNumber.toString()
                    } else
                        contact = user.email.toString()

                    var reportPic = urlTask.result.toString()
                    val currentTimestamp = System.currentTimeMillis()
                    var saveComplaint = Complaints(
                        user.uid,
                        user.displayName.toString(),
                        contact,
                        reportPic,
                        anonymousName,
                        vehicleNumber,
                        addressNameGet,
                        currentTimestamp,
                        this!!.latitutde!!,
                        this!!.longitude!!


                    )

                    val data = hashMapOf("numberOfPost" to numberOfPost + 1)

                    firestoreDB.collection("users").document(user.uid)
                        .set(data, SetOptions.merge())
                    firestoreViewModel?.saveComplaints(saveComplaint)
                    dialog.dismiss()
                    val intent = Intent(this, MainActivity::class.java).apply {
                        flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    Toasty.success(this, "Successful").show()

                }
            } else {
                uploadTask.exception?.let {
                    dialog.dismiss()

                    showError(it.message!!)
                }
            }
        }

    }

    private fun showError(errorMessage: String) {
        Toasty.error(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    fun isFormValid(): Boolean {
        if (address_name_get_edit_text.text.toString().trim().length == 0) {
            address_edit_input_get_locationLayout.error = "Enter Address"
            return false
        }
        return true

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
