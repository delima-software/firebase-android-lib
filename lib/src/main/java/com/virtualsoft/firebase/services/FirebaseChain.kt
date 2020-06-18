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
import com.virtualsoft.firebase.services.firestore.Firestore
import com.virtualsoft.firebase.services.firestore.FirestoreFactory

class FirebaseChain(context: Context? = null) : IServiceChain<IFirebase> {

    var authenticationProperties: Authentication.Properties? = null
    var firestoreProperties: Firestore.Properties? = null

    class Builder(val context: Context? = null) : IServiceChainBuilder<IFirebase> {

        override val building =
            FirebaseChain(context)

        init {
            building.factories.add(FirestoreFactory(context))
            building.factories.add(AuthenticationFactory(context))
            building.factories.add(AnalyticsFactory(context))
        }

        fun setAuthenticationProperties(authenticationProperties: Authentication.Properties?): Builder {
            building.authenticationProperties = authenticationProperties
            return this
        }

        fun setFirestoreProperties(firestoreProperties: Firestore.Properties?): Builder {
            building.firestoreProperties = firestoreProperties
            return this
        }

        override fun build(): FirebaseChain {
            val firestoreFactory = building.factories[0] as? FirestoreFactory
            firestoreFactory?.firestoreProperties = building.firestoreProperties

            val authenticationFactory = building.factories[1] as? AuthenticationFactory
            authenticationFactory?.authenticationProperties = building.authenticationProperties
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