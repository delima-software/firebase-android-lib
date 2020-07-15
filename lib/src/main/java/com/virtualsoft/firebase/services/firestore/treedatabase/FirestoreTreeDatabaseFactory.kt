package com.virtualsoft.firebase.services.firestore.treedatabase

import android.content.Context
import com.virtualsoft.core.designpatterns.chain.IChainRequest
import com.virtualsoft.core.service.IServiceFactory
import com.virtualsoft.firebase.IFirebase

class FirestoreTreeDatabaseFactory(var context: Context? = null,
                                   var firestoreTreeDatabaseProperties: FirestoreTreeDatabase.Properties? = null) : IServiceFactory<IFirebase> {

    private var builder =
        FirestoreTreeDatabase.Builder(context)

    override fun isProducer(request: IChainRequest): Boolean {
        return request.id == FirestoreTreeDatabase::class.java.name
    }

    override fun produce(): IFirebase {
        return builder
            .setFirestoreTreeDatabaseProperties(firestoreTreeDatabaseProperties)
            .build()
    }
}