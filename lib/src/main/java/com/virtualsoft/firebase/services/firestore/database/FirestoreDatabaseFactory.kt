package com.virtualsoft.firebase.services.firestore.database

import android.content.Context
import com.virtualsoft.core.designpatterns.chain.IChainRequest
import com.virtualsoft.core.service.IServiceFactory
import com.virtualsoft.firebase.IFirebase

class FirestoreDatabaseFactory (var firestoreDatabaseProperties: FirestoreDatabase.Properties? = null) : IServiceFactory<IFirebase> {
    private var builder = FirestoreDatabase.Builder()

    override fun isProducer(request: IChainRequest): Boolean {
        return request.id == FirestoreDatabase::class.java.name
    }

    override fun produce(): IFirebase {
        return builder
            .setFirestoreDatabaseProperties(firestoreDatabaseProperties)
            .build()
    }
}