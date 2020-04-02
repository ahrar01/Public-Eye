package com.qdesigns.publiceye.ui.home.TransitionRecognition

import android.content.Context

abstract class TransitionRecognitionAbstract {
    abstract fun startTracking(context: Context)
    abstract fun stopTracking()
}