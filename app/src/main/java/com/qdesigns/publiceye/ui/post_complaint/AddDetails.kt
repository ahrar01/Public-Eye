package com.qdesigns.publiceye.ui.post_complaint

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.qdesigns.publiceye.R
import com.qdesigns.publiceye.ui.home.MainActivity
import com.qdesigns.publiceye.utils.setLocalImage
import kotlinx.android.synthetic.main.activity_add_details.*
import java.io.File

class AddDetails : AppCompatActivity() {

    lateinit var imageFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_details)

        setSupportActionBar(toolbar_add_detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        imageFile = intent.extras?.get("imageFile") as File

        image_show.setLocalImage(imageFile)

        continue_button.setOnClickListener {
            val vehicle_number = vehicle_number_edit_text.text.toString().trim()
            if (isFormValid()) {
                var sendToAddDetails = Intent(this, GetLocation::class.java)
                sendToAddDetails.putExtra("imageFile", imageFile)
                sendToAddDetails.putExtra("vehicle_number", vehicle_number)

                startActivity(sendToAddDetails)
            }
        }

    }

    fun isFormValid(): Boolean {
        if (vehicle_number_edit_text.text.toString().trim().length == 0) {
            vehicle_number.error = "Enter Vehicle Number"
            return false
        }
        return true

    }

    override fun onSupportNavigateUp(): Boolean {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
        return true
    }
}
