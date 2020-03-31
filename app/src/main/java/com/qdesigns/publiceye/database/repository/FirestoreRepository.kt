package com.qdesigns.publiceye.database.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.qdesigns.publiceye.database.modal.UserInfo

class FirestoreRepository {
    val TAG = "FIREBASE_REPOSITORY"
    val firestoreDB: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    var user = FirebaseAuth.getInstance().currentUser!!

    //save user info
    fun saveUserInfo(userInfo: UserInfo): Task<Void> {
        var documentReference = firestoreDB.collection("users").document(user.uid)
        return documentReference.set(userInfo, SetOptions.merge())
    }

}