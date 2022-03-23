package com.virtualsoft.firebase.services.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.virtualsoft.core.designpatterns.builder.IBuilder

class Analytics : IAnalytics {

    private var firebaseAnalytics: FirebaseAnalytics? = null

    override val id: String
        get() = Analytics::class.java.name

    class Builder : IBuilder<Analytics> {

        override val building = Analytics()
    }

    override fun logEvent(context: Context, eventName: String, bundle: Bundle) {
        if (firebaseAnalytics == null)
            firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        firebaseAnalytics?.logEvent(eventName, bundle)
    }
}