package com.virtualsoft.firebase.services.firestore

import android.content.Context
import com.virtualsoft.core.designpatterns.chain.IChainRequest
import com.virtualsoft.core.service.IServiceFactory
import com.virtualsoft.firebase.IFirebase
import com.virtualsoft.firebase.services.authentication.IAuthentication

class FirestoreFactory(var context: Context? = null,
                       var firestoreProperties: Firestore.Properties? = null) : IServiceFactory<IFirebase> {

    private var builder =
        Firestore.Builder(context)

    override fun isProducer(request: IChainRequest): Boolean {
        return request.id == Firestore::class.java.name
    }

    override fun produce(): IFirebase {
        return builder
            .setFirestoreProperties(firestoreProperties)
            .build()
    }
}