package com.virtualsoft.firebase.services.messaging

import android.content.Context
import com.virtualsoft.core.designpatterns.chain.IChainRequest
import com.virtualsoft.core.service.IServiceFactory
import com.virtualsoft.firebase.IFirebase

class MessagingFactory(var context: Context? = null) : IServiceFactory<IFirebase> {

    private var builder = Messaging.Builder(context)

    override fun isProducer(request: IChainRequest): Boolean {
        return request.id == Messaging::class.java.name
    }

    override fun produce(): IFirebase {
        return builder.build()
    }
}