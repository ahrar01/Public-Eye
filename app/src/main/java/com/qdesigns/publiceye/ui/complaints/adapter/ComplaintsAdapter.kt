package com.qdesigns.publiceye.ui.complaints.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.qdesigns.publiceye.R
import com.qdesigns.publiceye.database.modal.Complaints
import com.qdesigns.publiceye.utils.DateTimeUtils
import com.qdesigns.publiceye.utils.GpsUtils
import kotlinx.android.synthetic.main.complaint_item.view.*

/**
 * RecyclerView adapter for a list of Complaints.
 */
open class ComplaintsAdapter(query: Query) :
    FirestoreAdapter<ComplaintsAdapter.ViewHolder>(query) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.complaint_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            snapshot: DocumentSnapshot
        ) {

            val complaints = snapshot.toObject<Complaints>()
            if (complaints == null) {
                return
            }

            val resources = itemView.resources

            // Load image
            Glide.with(itemView.reportPic.context)
                .load(complaints.reportPic)
                .into(itemView.reportPic)

            itemView.complaintAddress.text = complaints.address
            itemView.fullAddress.text = GpsUtils.getFullAddress(
                itemView.fullAddress.context,
                complaints.latitude,
                complaints.longitude
            )
            itemView.timestamp.text = DateTimeUtils.getDateString(complaints.submitedDate)
            itemView.vehicleRegNumber.text =
                "Vehicle Registration Number: " + complaints.vehicleNumber

            itemView.reason.text =
                "Reason : " + complaints.reasonLable

        }
    }
}