package com.qdesigns.publiceye.database.modal

import com.google.firebase.firestore.FieldValue

data class Complaints(
    var uid: String = "",
    var name: String = "",
    var contact: String = "",
    var reportPic: String = "",
    var anonymousName: String = "",
    var vehicleNumber: String = "",
    var address: String = "",
    var reasonLable: String = "",
    var submitedDate: Long = 0L,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
)