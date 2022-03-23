package com.virtualsoft.firebase.services.authentication

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.virtualsoft.core.designpatterns.builder.IBuilder

class Authentication : IAuthentication {

    data class Properties(var googleWebClientId: String? = null)

    private var authenticationProperties: Properties? = null
    private var googleSignInClient: GoogleSignInClient? = null

    override val id: String
        get() = Authentication::class.java.name

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    companion object {
        const val GOOGLE_SIGN_IN = 1
        const val EMAIL_SIGN_IN = 2
        const val EMAIL_SIGN_IN_TYPE_KEY = "email_sign_in_type_key"
        const val EMAIL_SIGN_IN_TYPE_CREATE = "create"
        const val EMAIL_SIGN_IN_TYPE_LOGIN = "login"
        const val EMAIL_KEY = "email"
        const val PASSWORD_KEY = "password"
    }

    class Builder : IBuilder<Authentication> {

        override val building = Authentication()

        fun setAuthenticationProperties(authenticationProperties: Properties?): Builder {
            building.authenticationProperties = authenticationProperties
            return this
        }
    }

    override fun configureSignIn(context: Context) {
        configureGoogleSignIn(context)
    }

    private fun configureGoogleSignIn(context: Context) {
        val googleWebClientId = authenticationProperties?.googleWebClientId
        if (googleWebClientId != null) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(googleWebClientId)
                .requestEmail()
                .build()
            googleSignInClient = GoogleSignIn.getClient(context, gso)
        }
    }

    override fun getGoogleSignInIntent(): Intent? {
        return googleSignInClient?.signInIntent
    }

    override fun authenticate(requestCode: Int, intent: Intent, callback: (Boolean) -> Unit) {
        when (requestCode) {
            GOOGLE_SIGN_IN -> authenticateWithGoogle(intent, callback)
            EMAIL_SIGN_IN -> authenticateWithEmail(intent, callback)
        }
    }

    private fun authenticateWithGoogle(intent: Intent, callback: (Boolean) -> Unit) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account!!, callback)
        } catch (e: ApiException) {
            callback(false)
        }
    }

    private fun authenticateWithEmail(intent: Intent, callback: (Boolean) -> Unit) {
        val email = intent.getStringExtra(EMAIL_KEY)
        val password = intent.getStringExtra(PASSWORD_KEY)
        if (email != null && password != null) {
            when (intent.getStringExtra(EMAIL_SIGN_IN_TYPE_KEY)) {
                EMAIL_SIGN_IN_TYPE_CREATE -> firebaseCreateWithEmailAndPassword(email, password, callback)
                EMAIL_SIGN_IN_TYPE_LOGIN -> firebaseLoginWithEmailAndPassword(email, password, callback)
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount, callback: (Boolean) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (loggedUser() != null)
                        callback(true)
                    else
                        callback(false)
                }
                else
                    callback(false)
            }
    }

    private fun firebaseCreateWithEmailAndPassword(email: String, password: String, callback: (Boolean) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (loggedUser() != null)
                        callback(true)
                    else
                        callback(false)
                }
                else
                    callback(false)
            }
    }

    private fun firebaseLoginWithEmailAndPassword(email: String, password: String, callback: (Boolean) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (loggedUser() != null)
                        callback(true)
                    else
                        callback(false)
                }
                else
                    callback(false)
            }
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    override fun loggedUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
}