package com.qdesigns.publiceye.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.qdesigns.publiceye.database.modal.Complaints
import com.qdesigns.publiceye.database.repository.FirestoreRepository
import com.qdesigns.publiceye.database.modal.UserInfo

class FirestoreViewModel(application: Application) : AndroidViewModel(application) {

    val TAG = "FIRESTORE_VIEW_MODEL"
    var firebaseRepository = FirestoreRepository()
    lateinit var savedUserInfo: UserInfo


    // save User Data to firebase
    fun saveUserData(userInfo: UserInfo) {
        firebaseRepository.saveUserInfo(userInfo).addOnFailureListener {
            Log.e(TAG, "Failed to save User Data!")
        }
    }

    // save User Data to firebase
    fun saveComplaints(complaints: Complaints) {
        firebaseRepository.saveComplainsInfo(complaints).addOnFailureListener {
            Log.e(TAG, "Failed to save User Data!")
        }
    }

}