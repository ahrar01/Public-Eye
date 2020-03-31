package com.qdesigns.publiceye

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.qdesigns.publiceye.ui.UserDetails
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        willDeleteTHis.setOnClickListener {
            startActivity(Intent(this, UserDetails::class.java))
        }
    }
}
