package com.virtualsoft.firebase.services.analytics

import android.content.Context
import android.os.Bundle
import com.virtualsoft.firebase.IFirebase

interface IAnalytics : IFirebase {

    var context: Context?

    fun logEvent(eventName: String, bundle: Bundle)
}