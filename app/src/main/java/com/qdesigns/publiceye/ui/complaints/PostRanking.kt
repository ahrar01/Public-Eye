package com.qdesigns.publiceye.ui.complaints

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.qdesigns.publiceye.R
import com.qdesigns.publiceye.ui.complaints.adapter.RankingAdapter
import kotlinx.android.synthetic.main.activity_post_ranking.*

class PostRanking : AppCompatActivity() {
    lateinit var firestore: FirebaseFirestore
    lateinit var query: Query
    lateinit var adapter: RankingAdapter
    var user = FirebaseAuth.getInstance().currentUser!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_ranking)
        // Firestore
        firestore = Firebase.firestore

        // Get ${LIMIT} complaints
        query = firestore.collection("users")
            .whereGreaterThan("numberOfPost", 0)
            .orderBy("numberOfPost", Query.Direction.DESCENDING)
            .limit(LIMIT.toLong())

        // RecyclerView
        adapter = object : RankingAdapter(query) {
            override fun onDataChanged() {
                // Show/hide content if the query returns empty.
                if (itemCount == 0) {
                    ranking_RecyclerView.visibility = View.GONE
                    no_ranking_tv.visibility = View.VISIBLE
                } else {
                    ranking_RecyclerView.visibility = View.VISIBLE
                    no_ranking_tv.visibility = View.GONE
                }
            }

            override fun onError(e: FirebaseFirestoreException) {
                // Show a snackbar on errors
                Log.e(TAG, "${e.message}")
            }
        }

        ranking_RecyclerView.layoutManager = LinearLayoutManager(this)
        ranking_RecyclerView.adapter = adapter
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

        private const val TAG = "RankingActivity"

        private const val LIMIT = 10
    }
}
