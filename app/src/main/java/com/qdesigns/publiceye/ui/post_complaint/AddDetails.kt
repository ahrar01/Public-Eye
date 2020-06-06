package com.qdesigns.publiceye.ui.post_complaint

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.custom.*
import com.qdesigns.publiceye.R
import com.qdesigns.publiceye.ui.home.MainActivity
import com.qdesigns.publiceye.utils.setLocalImage
import kotlinx.android.synthetic.main.activity_add_details.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class AddDetails : AppCompatActivity() {
    private val TAG = "ADD_DETAILS"

    lateinit var imageFile: File
    var anonymousName = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_details)

        setSupportActionBar(toolbar_add_detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        imageFile = intent.extras?.get("imageFile") as File
        anonymousName = intent.extras?.get("anonymousName") as String
        image_show.setLocalImage(imageFile)

        continue_button.setOnClickListener {
            val vehicle_number = vehicle_number_edit_text.text.toString().trim()
            if (isFormValid()) {
                var sendToAddDetails = Intent(this, GetLocation::class.java)
                sendToAddDetails.putExtra("imageFile", imageFile)
                sendToAddDetails.putExtra("vehicle_number", vehicle_number)
                sendToAddDetails.putExtra("anonymousName", anonymousName)


                startActivity(sendToAddDetails)
            }
        }


        val localModel =
            FirebaseCustomLocalModel.Builder() //for loading model from local assets folder
                .setAssetFilePath("model_unquant.tflite")
                .build()

        val remoteModel = FirebaseCustomRemoteModel.Builder("model_unquant").build()

        val conditions = FirebaseModelDownloadConditions.Builder()
            .requireWifi()
            .build()
        FirebaseModelManager.getInstance().download(remoteModel, conditions)
            .addOnCompleteListener {
                // Success.
                Log.d(TAG, "model downloaded")

            }

        var interpreter: FirebaseModelInterpreter
        FirebaseModelManager.getInstance().isModelDownloaded(remoteModel)
            .addOnSuccessListener { isDownloaded ->
                val options =
                    if (isDownloaded) {
                        FirebaseModelInterpreterOptions.Builder(remoteModel).build()
                    } else {
                        FirebaseModelInterpreterOptions.Builder(localModel).build()
                    }
                interpreter = FirebaseModelInterpreter.getInstance(options)!!
                identifyImage(interpreter)
            }

    }

    private fun identifyImage(interpreter: FirebaseModelInterpreter) {

        val inputOutputOptions = FirebaseModelInputOutputOptions.Builder()
            .setInputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 224, 224, 3))
            .setOutputFormat(
                0,
                FirebaseModelDataType.FLOAT32,
                intArrayOf(1, 2)
            ) // here replace 2 with no of class added in your model , for production apps you can read the labels.txt files here and to get no of classes dynamically
            .build()


        val bitmap = BitmapFactory.decodeFile(imageFile.path)

        val batchNum = 0
        val input = Array(1) { Array(224) { Array(224) { FloatArray(3) } } }
        for (x in 0..223) {
            for (y in 0..223) {
                val pixel = bitmap.getPixel(x, y)
                // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                // model. For example, some models might require values to be normalized
                // to the range [0.0, 1.0] instead.
                input[batchNum][x][y][0] = (Color.red(pixel) - 127) / 255.0f
                input[batchNum][x][y][1] = (Color.green(pixel) - 127) / 255.0f
                input[batchNum][x][y][2] = (Color.blue(pixel) - 127) / 255.0f
            }
        }

        val inputs = FirebaseModelInputs.Builder()
            .add(input) // add() as many input arrays as your model requires
            .build()
        interpreter.run(inputs, inputOutputOptions)
            .addOnSuccessListener { result ->
                // ...
                val output = result.getOutput<Array<FloatArray>>(0)
                val probabilities = output[0]
                val reader = BufferedReader(
                    InputStreamReader(assets.open("labels.txt"))
                )
                var higherProbablityFloat = 0F
                for (i in probabilities.indices) {

                    if (higherProbablityFloat < probabilities[i]) {
                        val label = reader.readLine()
                        higherProbablityFloat = probabilities[i]
                        Log.d(
                            TAG,
                            "The Image is of ${label.substring(2)} , confidance : $higherProbablityFloat"
                        )
                    }
                }
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                // ...
                Log.d(TAG, "exception ${e.message}")

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
