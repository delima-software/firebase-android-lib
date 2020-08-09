package com.virtualsoft.firebase.services.storage

import android.content.Context
import com.virtualsoft.core.designpatterns.chain.IChainRequest
import com.virtualsoft.core.service.IServiceFactory
import com.virtualsoft.firebase.IFirebase

class StorageFactory(var context: Context? = null) : IServiceFactory<IFirebase> {

    private var builder = Storage.Builder()

    override fun isProducer(request: IChainRequest): Boolean {
        return request.id == Storage::class.java.name
    }

    override fun produce(): IFirebase {
        return builder.build()
    }
}