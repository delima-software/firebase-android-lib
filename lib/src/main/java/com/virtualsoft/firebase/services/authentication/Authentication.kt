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

class Authentication(override var context: Context? = null) :
    IAuthentication {

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
    }

    class Builder(context: Context?) : IBuilder<Authentication> {

        override val building =
            Authentication(
                context
            )

        fun setAuthenticationProperties(authenticationProperties: Properties?): Builder {
            building.authenticationProperties = authenticationProperties
            return this
        }
    }

    override fun configureSignIn() {
        configureGoogleSignIn()
    }

    private fun configureGoogleSignIn() {
        context?.let {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(authenticationProperties?.googleWebClientId)
                .requestEmail()
                .build()
            googleSignInClient = GoogleSignIn.getClient(it, gso)
        }
    }

    override fun getGoogleSignInIntent(): Intent? {
        return googleSignInClient?.signInIntent
    }

    override fun authenticate(requestCode: Int, intent: Intent, callback: (Boolean) -> Unit) {
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!, callback)
            } catch (e: ApiException) {
                callback(false)
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

    override fun signOut() {
        firebaseAuth.signOut()
    }

    override fun loggedUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
}