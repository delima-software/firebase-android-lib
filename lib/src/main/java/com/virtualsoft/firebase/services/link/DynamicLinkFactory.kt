package com.virtualsoft.firebase.services.link

import android.content.Context
import com.virtualsoft.core.designpatterns.chain.IChainRequest
import com.virtualsoft.core.service.IServiceFactory
import com.virtualsoft.firebase.IFirebase

class DynamicLinkFactory(var dynamicLinkProperties: DynamicLink.Properties? = null) : IServiceFactory<IFirebase> {

    private var builder = DynamicLink.Builder()

    override fun isProducer(request: IChainRequest): Boolean {
        return request.id == DynamicLink::class.java.name
    }

    override fun produce(): IFirebase {
        return builder
            .setDynamicLinkProperties(dynamicLinkProperties)
            .build()
    }
}