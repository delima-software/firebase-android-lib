package com.virtualsoft.firebase.services.authentication

import android.content.Context
import android.content.Intent
import com.google.firebase.auth.FirebaseUser
import com.virtualsoft.firebase.IFirebase

interface IAuthentication : IFirebase {

    //Authentication
    fun configureSignIn(context: Context)

    fun getGoogleSignInIntent(): Intent?

    fun authenticate(requestCode: Int, intent: Intent, callback: (Boolean) -> Unit)

    fun signOut()

    fun loggedUser(): FirebaseUser?
}