package com.qdesigns.publiceye.ui.home

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.firebase.ui.auth.AuthUI
import com.qdesigns.publiceye.R
import com.qdesigns.publiceye.ui.auth.AuthActivity
import com.qdesigns.publiceye.ui.auth.SaveUserDetails
import com.qdesigns.publiceye.ui.home.TransitionRecognition.TransitionRecognition
import com.qdesigns.publiceye.ui.home.TransitionRecognition.TransitionRecognitionUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var mTransitionRecognition: TransitionRecognition

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initTransitionRecognition()

        willDeleteTHis.setOnClickListener {
            signOut()
        }
    }

    override fun onResume() {
        super.onResume()
        showPreviousTransitions()
    }

    override fun onPause() {
        mTransitionRecognition.stopTracking()
        super.onPause()
    }

    /**
     * INIT TRANSITION RECOGNITION
     */
    fun initTransitionRecognition() {
        mTransitionRecognition = TransitionRecognition()
        mTransitionRecognition.startTracking(this)
    }

    /**
     * Show previous transitions. This is an example to explain how to detect user's activity. To
     * see this activity we have to relaunch the app.
     */
    fun showPreviousTransitions() {
        val sharedPref = getSharedPreferences(
            TransitionRecognitionUtils.SHARED_PREFERENCES_FILE_KEY_TRANSITIONS, Context.MODE_PRIVATE
        )

        var previousTransitions =
            sharedPref.getString(TransitionRecognitionUtils.SHARED_PREFERENCES_KEY_TRANSITIONS, "")

        main_activity_tv.text = previousTransitions
    }

    fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                startActivity(
                    Intent(this, AuthActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                finish()
            }
    }
}
