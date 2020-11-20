package com.virtualsoft.firebase.services.messaging

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.virtualsoft.core.designpatterns.builder.IBuilder
import com.virtualsoft.firebase.utils.LogUtils
import kotlinx.coroutines.tasks.await

class Messaging(var context: Context? = null) : IMessaging {

    override val id = Messaging::class.java.name

    class Builder(context: Context?): IBuilder<IMessaging> {

        override val building = Messaging(context)
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