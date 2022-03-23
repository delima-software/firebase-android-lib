package com.virtualsoft.firebase.services

import android.content.Context
import com.virtualsoft.core.designpatterns.chain.IChainRequest
import com.virtualsoft.core.service.IServiceFactory
import com.virtualsoft.core.service.chain.IServiceChain
import com.virtualsoft.core.service.chain.IServiceChainBuilder
import com.virtualsoft.firebase.IFirebase
import com.virtualsoft.firebase.services.analytics.AnalyticsFactory
import com.virtualsoft.firebase.services.authentication.AuthenticationFactory
import com.virtualsoft.firebase.services.authentication.Authentication
import com.virtualsoft.firebase.services.firestore.database.FirestoreDatabase
import com.virtualsoft.firebase.services.firestore.database.FirestoreDatabaseFactory
import com.virtualsoft.firebase.services.link.DynamicLink
import com.virtualsoft.firebase.services.link.DynamicLinkFactory
import com.virtualsoft.firebase.services.messaging.MessagingFactory
import com.virtualsoft.firebase.services.storage.StorageFactory

class FirebaseChain : IServiceChain<IFirebase> {

    var firestoreDatabaseProperties: FirestoreDatabase.Properties? = null
    var authenticationProperties: Authentication.Properties? = null
    var dynamicLinkProperties: DynamicLink.Properties? = null

    class Builder : IServiceChainBuilder<IFirebase> {

        override val building = FirebaseChain()

        init {
            building.factories.add(FirestoreDatabaseFactory())
            building.factories.add(AuthenticationFactory())
            building.factories.add(DynamicLinkFactory())
            building.factories.add(AnalyticsFactory())
            building.factories.add(StorageFactory())
            building.factories.add(MessagingFactory())
        }

        fun setAuthenticationProperties(authenticationProperties: Authentication.Properties?): Builder {
            building.authenticationProperties = authenticationProperties
            return this
        }

        fun setFirestoreDatabaseProperties(firestoreDatabaseProperties: FirestoreDatabase.Properties?): Builder {
            building.firestoreDatabaseProperties = firestoreDatabaseProperties
            return this
        }

        fun setDynamicLinkProperties(dynamicLinkProperties: DynamicLink.Properties?): Builder {
            building.dynamicLinkProperties = dynamicLinkProperties
            return this
        }

        override fun build(): FirebaseChain {
            val firestoreDatabaseFactory = building.factories[0] as? FirestoreDatabaseFactory
            firestoreDatabaseFactory?.firestoreDatabaseProperties = building.firestoreDatabaseProperties

            val authenticationFactory = building.factories[2] as? AuthenticationFactory
            authenticationFactory?.authenticationProperties = building.authenticationProperties

            val dynamicLinkFactory = building.factories[3] as? DynamicLinkFactory
            dynamicLinkFactory?.dynamicLinkProperties = building.dynamicLinkProperties

            return building
        }
    }

    private val factories = mutableListOf<IServiceFactory<IFirebase>>()

    override fun resolve(request: IChainRequest): IFirebase? {
        factories.forEach { factory ->
            if (factory.isProducer(request))
                return factory.produce()
        }
        return null
    }
}