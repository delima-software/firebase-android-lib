package com.virtualsoft.firebase.services.firestore

import android.content.Context
import com.virtualsoft.firebase.IFirebase

interface IFirestore : IFirebase {

    var context: Context?

    companion object {

        fun metadataCollection(): String {
            return "metadata"
        }
    }
}