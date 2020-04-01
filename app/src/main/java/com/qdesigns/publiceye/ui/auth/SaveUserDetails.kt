package com.qdesigns.publiceye.ui.auth

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.qdesigns.publiceye.R
import com.qdesigns.publiceye.database.modal.UserInfo
import com.qdesigns.publiceye.ui.home.MainActivity
import com.qdesigns.publiceye.utils.GpsUtils
import com.qdesigns.publiceye.utils.setProgressDialog
import com.qdesigns.publiceye.viewmodel.FirestoreViewModel
import com.rommansabbir.locationlistenerandroid.LocationCallback
import com.rommansabbir.locationlistenerandroid.LocationListener
import com.rommansabbir.locationlistenerandroid.PermissionCallback
import com.theartofdev.edmodo.cropper.CropImage
import es.dmoral.toasty.Toasty
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.android.synthetic.main.activity_user_details.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File

class SaveUserDetails : AppCompatActivity(), PermissionCallback {

    val TAG = "USER_INFO"

    var IMAGE_STATUS = false
    private var compressedImage: File? = null
    private var thumb_image: Bitmap? = null
    var user = FirebaseAuth.getInstance().currentUser!!
    var firestoreViewModel: FirestoreViewModel? = null
    var latitutde: Double? = null
    var longitude: Double? = null

    var currentAddress: String = ""

    private lateinit var gpsUtils: GpsUtils


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)
        gpsUtils = GpsUtils(this)
        /**
         * Set LocationListener component with activity
         */
        LocationListener.setComponent(this)

        firestoreViewModel = ViewModelProvider(this).get(FirestoreViewModel::class.java)
        setupClickListener()

    }


    private fun setupClickListener() {
        selectProfilePic.setOnClickListener(View.OnClickListener { view ->
            Dexter.withActivity(this)
                .withPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) { // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) { // do you work now
                            val intent = Intent(Intent.ACTION_GET_CONTENT)
                            intent.type = "image/*"
                            startActivityForResult(intent, 1001)
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied) { // permission is denied permenantly, navigate user to app settings
                            Snackbar.make(
                                view,
                                "Kindly grant Required Permission",
                                Snackbar.LENGTH_LONG
                            )
                                .setAction("Allow", null).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: List<PermissionRequest>,
                        token: PermissionToken
                    ) {
                        token.continuePermissionRequest()
                    }
                })
                .onSameThread()
                .check()
            //result will be available in onActivityResult which is overridden
        })

        address_edit_input_layout.setEndIconOnClickListener {
            gpsUtils.getLatLong { lat, long ->
                println("location is $lat + $long")
            }
            LocationListener.getLocation(object : LocationCallback {
                override fun onLocationSuccess(location: Location) {

                    latitutde = location.latitude
                    longitude = location.longitude

                    currentAddress = gpsUtils.getAddress(
                        this@SaveUserDetails,
                        location.latitude,
                        location.longitude
                    )!!
                    address_name_edit_text.setText(currentAddress)
                }
            })
            Log.d(TAG, "location is $latitutde + $longitude  address: $currentAddress")


        }

        upload_user_btn.setOnClickListener {

            val Name = create_name_edit_text.text.toString().trim()
            val anonymousName = anonymous_name_edit_text.text.toString().trim()

            if (isFormValid()) {
                uploadImageAndSaveUri(Name, anonymousName, thumb_image)
            }

        }

    }


    fun isFormValid(): Boolean {
        if (IMAGE_STATUS == false) {
            Toasty.info(this, "Select A Profile Picture", Toast.LENGTH_LONG).show()
            return false
        }
        if (create_name_edit_text.text.toString().trim().length == 0) {
            name_input_layout.error = "Enter Your Name"
            return false
        }
        if (anonymous_name_edit_text.text.toString().trim().length == 0) {
            anonymous_name_edit_input.error = "Enter Anonymous Name"
            return false
        }

        if (latitutde == null || longitude == null && address_name_edit_text.text.toString()
                .trim().length == 0
        ) {
            Toasty.info(this, "Please Click on Location Button", Toast.LENGTH_LONG).show()
            return false
        }
        return true

    }


    private fun uploadImageAndSaveUri(name: String, anonymousName: String, thumbImage: Bitmap?) {
        val dialog = setProgressDialog(this)
        dialog.show()
        val baos = ByteArrayOutputStream()
        val storageRef = FirebaseStorage.getInstance()
            .reference
            .child("profilePics/${FirebaseAuth.getInstance().currentUser?.uid}")
        thumbImage?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val image = baos.toByteArray()

        val upload = storageRef.putBytes(image)

        upload.addOnCompleteListener { uploadTask ->
            if (uploadTask.isSuccessful) {
                storageRef.downloadUrl.addOnCompleteListener { urlTask ->
                    var contact = ""
                    if (user.email.isNullOrEmpty()) {
                        contact = user.phoneNumber.toString()
                    } else
                        contact = user.email.toString()

                    var profile_pic = urlTask.result.toString()
                    var userValues = UserInfo(
                        user.uid,
                        name,
                        contact,
                        profile_pic,
                        anonymousName,
                        currentAddress
                    )
                    firestoreViewModel?.saveUserData(userValues)

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .setPhotoUri(Uri.parse(profile_pic))
                        .build()

                    user.updateProfile(profileUpdates)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // SharedPreferencesDB.savePreferredUser(this, userValues)
                                dialog.dismiss()

                                val intent = Intent(this, MainActivity::class.java).apply {
                                      flags =
                                          Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                  }
                                startActivity(intent)
                                Log.d(TAG, "User profile updated.")
                            } else {
                                Log.d(TAG, "User profile is not updated.")

                            }
                        }

                }
            } else {
                uploadTask.exception?.let {
                    showError(it.message!!)
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {

            if (data == null) {
                showError("Failed to open picture!")
                return
            }
            val imageUri = data.data //Geting uri of the data
            IMAGE_STATUS = true //setting the flag
            CropImage.activity(imageUri)
                .setAspectRatio(1, 1)
                .setMinCropWindowSize(500, 500)
                .start(this)
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {

                val thumb_filePath = File(result.uri.path)

                thumb_filePath.let { imageFile ->
                    lifecycleScope.launch {
                        // Full custom
                        compressedImage = Compressor.compress(this@SaveUserDetails, imageFile) {
                            resolution(200, 200)
                            quality(75)
                            format(Bitmap.CompressFormat.WEBP)
                        }
                        compressedImage?.let {
                            thumb_image = BitmapFactory.decodeFile(it.absolutePath)
                            profilePic.setImageBitmap(thumb_image)
                        }

                    }
                } ?: showError("Please choose an image!")


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                // showError(result.error)
            }
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
                        this@SaveUserDetails,
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

    private fun showError(errorMessage: String) {
        Toasty.error(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

}
