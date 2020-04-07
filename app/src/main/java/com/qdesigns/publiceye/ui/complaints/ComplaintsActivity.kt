package com.qdesigns.publiceye.ui.complaints

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.qdesigns.publiceye.R
import com.qdesigns.publiceye.ui.complaints.adapter.ComplaintsAdapter
import kotlinx.android.synthetic.main.activity_complaints.*


class ComplaintsActivity : AppCompatActivity() {

    lateinit var firestore: FirebaseFirestore
    lateinit var query: Query
    lateinit var adapter: ComplaintsAdapter
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

        // Firestore
        firestore = Firebase.firestore

        // Get ${LIMIT} complaints
        query = firestore.collection("complaints")
            .orderBy("submitedDate", Query.Direction.DESCENDING)
            .limit(LIMIT.toLong())

        // RecyclerView
        adapter = object : ComplaintsAdapter(query) {
            override fun onDataChanged() {
                // Show/hide content if the query returns empty.
                if (itemCount == 0) {
                    complaints_RecyclerView.visibility = View.GONE
                    no_complaints_tv.visibility = View.VISIBLE
                } else {
                    complaints_RecyclerView.visibility = View.VISIBLE
                    no_complaints_tv.visibility = View.GONE
                }
            }

            override fun onError(e: FirebaseFirestoreException) {
                // Show a snackbar on errors
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Error: check logs for info.", Snackbar.LENGTH_LONG
                ).show()
            }
        }

        complaints_RecyclerView.layoutManager = LinearLayoutManager(this)
        complaints_RecyclerView.adapter = adapter
    }

    public override fun onStart() {
        super.onStart()

        // Start listening for Firestore updates
        adapter.startListening()
    }

    public override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    companion object {

        private const val TAG = "ComplaintsActivity"

        private const val LIMIT = 10
    }
}
