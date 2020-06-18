package com.virtualsoft.firebase.services.authentication

import android.content.Context
import android.content.Intent
import com.google.firebase.auth.FirebaseUser
import com.virtualsoft.firebase.IFirebase

interface IAuthentication : IFirebase {

    var context: Context?

    //Authentication
    fun configureSignIn()

    fun getGoogleSignInIntent(): Intent?

    fun authenticate(requestCode: Int, intent: Intent, callback: (Boolean) -> Unit)

    fun signOut()

    fun loggedUser(): FirebaseUser?
}