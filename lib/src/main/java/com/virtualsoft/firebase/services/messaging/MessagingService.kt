package com.virtualsoft.firebase.services.messaging

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {

    companion object {
        var messagingProperties: MessagingServiceProperties? = null
            internal set
    }

    data class MessagingServiceProperties(val messagingCallback: ((RemoteMessage) -> Unit)? = null,
                                          val tokenCallback: ((String) -> Unit)? = null)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        messagingProperties?.tokenCallback?.invoke(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        messagingProperties?.messagingCallback?.invoke(message)
    }
}