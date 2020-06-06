package com.qdesigns.publiceye.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.android.gms.location.ActivityRecognitionClient
import com.qdesigns.publiceye.ui.home.MainActivity

class BackgroundDetectedActivitiesService : Service() {

    private lateinit var mIntentService: Intent
    private lateinit var mPendingIntent: PendingIntent
    private lateinit var mActivityRecognitionClient: ActivityRecognitionClient

    internal var mBinder: IBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        val serverInstance: BackgroundDetectedActivitiesService
            get() = this@BackgroundDetectedActivitiesService
    }

    override fun onCreate() {
        super.onCreate()
        mActivityRecognitionClient = ActivityRecognitionClient(this)
        mIntentService = Intent(this, DetectedActivitiesIntentService::class.java)
        mPendingIntent =
            PendingIntent.getService(this, 1, mIntentService, PendingIntent.FLAG_UPDATE_CURRENT)
        requestActivityUpdatesButtonHandler()
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return Service.START_STICKY
    }

    fun requestActivityUpdatesButtonHandler() {
        val task = mActivityRecognitionClient.requestActivityUpdates(
            MainActivity.DETECTION_INTERVAL_IN_MILLISECONDS,
            mPendingIntent
        )

        task?.addOnSuccessListener {
            Log.d(TAG, "Successfully requested activity updates")

        }

        task?.addOnFailureListener {
            Log.d(TAG, "Requesting activity updates failed to start")

        }
    }

    fun removeActivityUpdatesButtonHandler() {
        val task = mActivityRecognitionClient.removeActivityUpdates(
            mPendingIntent
        )
        task?.addOnSuccessListener {
            Log.d(TAG, "Removed activity updates successfully!")
        }

        task?.addOnFailureListener {
            Log.d(TAG, "Failed to remove activity updates!")

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeActivityUpdatesButtonHandler()
    }

    companion object {
        private val TAG = BackgroundDetectedActivitiesService::class.java.simpleName
    }
}