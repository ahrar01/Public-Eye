package com.qdesigns.publiceye.ui.auth

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.qdesigns.publiceye.ui.home.MainActivity
import com.qdesigns.publiceye.R
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity : AppCompatActivity() {

    val RC_SIGN_IN = 1
    val TAG = "AUTH_UI"
    var mAuth: FirebaseAuth? = null
    var firestoreDB = FirebaseFirestore.getInstance()
    var collectionReference = firestoreDB.collection("users")
    private var flag: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        title = "Sign In"

        mAuth = FirebaseAuth.getInstance()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        g_plus_button.setOnClickListener {
            chooseAuthProviders()
        }

        phone_number_button.setOnClickListener {
            startActivity(Intent(this, VerifyPhoneActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        mAuth = FirebaseAuth.getInstance()
        var user = mAuth!!.currentUser
        if (user == null)
        //chooseAuthProviders()

        else
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
    }

    override fun onResume() {
        super.onResume()
        mAuth = FirebaseAuth.getInstance()
        var user = mAuth!!.currentUser
        if (user == null)
        //chooseAuthProviders()
        else
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
    }

    var customLayout = AuthMethodPickerLayout.Builder(R.layout.activity_auth)
        .setGoogleButtonId(R.id.g_plus_button)
        .build()

    fun chooseAuthProviders() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build(),
            RC_SIGN_IN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                var user = FirebaseAuth.getInstance().currentUser!!

                val query = collectionReference.whereEqualTo("uid", user.uid)
                query.get()
                    .addOnSuccessListener { queryDocumentSnapshots ->
                        for (documentSnapshot in queryDocumentSnapshots) {
                            flag = true
                        }
                        if (flag) {
                            val intent = Intent(this, MainActivity::class.java).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)

                        } else {
                            val intent = Intent(this, SaveUserDetails::class.java).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                        }
                    }
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                Log.e(TAG, "Sign In Failed. ")
            }
        }
    }


}
