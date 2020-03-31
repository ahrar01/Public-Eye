package com.qdesigns.publiceye.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.qdesigns.publiceye.database.repository.FirestoreRepository
import com.qdesigns.publiceye.database.modal.UserInfo

class FirestoreViewModel(application: Application) : AndroidViewModel(application) {

    val TAG = "FIRESTORE_VIEW_MODEL"
    var firebaseRepository = FirestoreRepository()


    // save User Data to firebase
    fun saveUserData(userInfo: UserInfo) {
        firebaseRepository.saveUserInfo(userInfo).addOnFailureListener {
            Log.e(TAG, "Failed to save User Data!")
        }
    }
}