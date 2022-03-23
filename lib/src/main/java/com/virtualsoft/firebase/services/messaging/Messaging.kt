package com.virtualsoft.firebase.services.messaging

import com.google.firebase.messaging.FirebaseMessaging
import com.virtualsoft.core.designpatterns.builder.IBuilder
import com.virtualsoft.firebase.utils.LogUtils
import kotlinx.coroutines.tasks.await

class Messaging : IMessaging {

    override val id = Messaging::class.java.name

    class Builder : IBuilder<IMessaging> {

        override val building = Messaging()
    }

    override suspend fun getToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        }
        catch (e: Exception) {
            LogUtils.logError("TOKEN", "could not get firebase token", e)
            null
        }
    }
}