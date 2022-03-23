package com.virtualsoft.firebase.services.messaging

import android.content.Context
import com.virtualsoft.core.designpatterns.chain.IChainRequest
import com.virtualsoft.core.service.IServiceFactory
import com.virtualsoft.firebase.IFirebase

class MessagingFactory : IServiceFactory<IFirebase> {

    private var builder = Messaging.Builder()

    override fun isProducer(request: IChainRequest): Boolean {
        return request.id == Messaging::class.java.name
    }

    override fun produce(): IFirebase {
        return builder.build()
    }
}