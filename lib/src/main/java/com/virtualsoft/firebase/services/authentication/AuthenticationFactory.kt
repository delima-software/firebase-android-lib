package com.virtualsoft.firebase.services.authentication

import android.content.Context
import com.virtualsoft.core.designpatterns.chain.IChainRequest
import com.virtualsoft.core.service.IServiceFactory
import com.virtualsoft.firebase.IFirebase

class AuthenticationFactory(var authenticationProperties: Authentication.Properties? = null) : IServiceFactory<IFirebase> {

    private var builder = Authentication.Builder()

    override fun isProducer(request: IChainRequest): Boolean {
        return request.id == Authentication::class.java.name
    }

    override fun produce(): IFirebase {
        return builder
            .setAuthenticationProperties(authenticationProperties)
            .build()
    }
}