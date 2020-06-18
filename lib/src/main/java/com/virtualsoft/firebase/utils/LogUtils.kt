package com.virtualsoft.firebase.utils

import android.util.Log

object LogUtils {

    fun logSuccess(tag: String, message: String) {
        Log.i(tag, message)
    }

    fun logError(tag: String, message: String, error: Throwable? = null) {
        if (error != null)
            Log.e(tag, message, error)
        else
            Log.d(tag, message)
    }
}