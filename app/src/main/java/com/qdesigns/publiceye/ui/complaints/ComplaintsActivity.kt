package com.qdesigns.publiceye.ui.complaints

import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
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
    var user = FirebaseAuth.getInstance().currentUser!!
    private val TAG = ComplaintsActivity::class.java.simpleName

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

        Filterbtn.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                if (position == 0) {
                    onFilter(0)
                }
                if (position == 1) {
                    onFilter(1)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }

        }

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
                Log.e(TAG, "${e.message}")
            }
        }

        complaints_RecyclerView.layoutManager = LinearLayoutManager(this)
        complaints_RecyclerView.adapter = adapter
    }

    fun onFilter(number: Int) {
        // Construct query basic query
        var query: Query = firestore.collection("complaints")

        if (number == 0) {
            query = query.orderBy("submitedDate", Query.Direction.DESCENDING)

        }

        if (number == 1) {
            query = query.whereEqualTo("uid", user.uid)
                .orderBy("submitedDate", Query.Direction.DESCENDING)
        }

        // Limit items
        query = query.limit(LIMIT.toLong())

        // Update the query
        adapter.setQuery(query)

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
