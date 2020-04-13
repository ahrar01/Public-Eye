package com.qdesigns.publiceye.ui.complaints.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.qdesigns.publiceye.R
import com.qdesigns.publiceye.database.modal.UserInfo
import kotlinx.android.synthetic.main.single_user.view.*

/**
 * RecyclerView adapter for a list of Complaints.
 */
open class RankingAdapter(query: Query) :
    FirestoreAdapter<RankingAdapter.RankingViewHolder>(query) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return RankingViewHolder(inflater.inflate(R.layout.single_user, parent, false))
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        holder.bind(getSnapshot(position))
    }

    class RankingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            snapshot: DocumentSnapshot
        ) {

            val rankingUsers = snapshot.toObject<UserInfo>()
            if (rankingUsers == null) {
                return
            }

            val resources = itemView.resources
            val options: RequestOptions = RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.user_placeholder)
                .error(R.drawable.user_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)
                .dontAnimate()
                .dontTransform()
            // Load image
            Glide.with(itemView.user_list_image.context)
                .applyDefaultRequestOptions(options)
                .load(rankingUsers.profilePic)
                .into(itemView.user_list_image)



            itemView.user_list_name.text = rankingUsers.anonymousName
            itemView.user_no_of_complaints.text =
                "Registered Complaints : " + rankingUsers.numberOfPost

        }
    }
}