package com.virtualsoft.firebase.services.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.virtualsoft.core.designpatterns.builder.IBuilder

class Analytics(override var context: Context? = null) : IAnalytics {

    private var firebaseAnalytics: FirebaseAnalytics? = null

    init {
        context?.let {
            firebaseAnalytics = FirebaseAnalytics.getInstance(it)
        }
    }

    override val id: String
        get() = Analytics::class.java.name

    class Builder(context: Context?) : IBuilder<Analytics> {

        override val building = Analytics(context)
    }

    override fun logEvent(eventName: String, bundle: Bundle) {
        firebaseAnalytics?.logEvent(eventName, bundle)
    }
}