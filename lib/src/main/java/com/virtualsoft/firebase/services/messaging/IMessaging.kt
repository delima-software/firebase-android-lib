package com.virtualsoft.firebase.services.messaging

import com.virtualsoft.firebase.IFirebase

interface IMessaging : IFirebase {

    suspend fun getToken(): String?
}