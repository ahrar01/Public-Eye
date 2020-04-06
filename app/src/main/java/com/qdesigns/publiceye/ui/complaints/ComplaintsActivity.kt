package com.qdesigns.publiceye.ui.complaints

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.qdesigns.publiceye.R
import com.qdesigns.publiceye.database.modal.Complaints
import kotlinx.android.synthetic.main.activity_complaints.*


class ComplaintsActivity : AppCompatActivity() {

 
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complaints)

        // access the items of the list
        val sort = resources.getStringArray(R.array.sort)
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, sort
        )
        Filterbtn.adapter = spinnerAdapter


    }

}
