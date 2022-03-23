package com.virtualsoft.firebase.services.analytics

import android.content.Context
import android.os.Bundle
import com.virtualsoft.firebase.IFirebase

interface IAnalytics : IFirebase {

    fun logEvent(context: Context, eventName: String, bundle: Bundle)
}