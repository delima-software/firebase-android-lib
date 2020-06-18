package com.virtualsoft.firebase.services.analytics

import android.content.Context
import com.virtualsoft.core.designpatterns.chain.IChainRequest
import com.virtualsoft.core.service.IServiceFactory
import com.virtualsoft.firebase.IFirebase

class AnalyticsFactory(var context: Context? = null) : IServiceFactory<IFirebase> {

    private var builder = Analytics.Builder(context)

    override fun isProducer(request: IChainRequest): Boolean {
        return request.id == Analytics::class.java.name
    }

    override fun produce(): IFirebase {
        return builder.build()
    }
}