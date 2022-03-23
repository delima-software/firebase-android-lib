package com.virtualsoft.firebase.services.firestore

import com.virtualsoft.firebase.IFirebase

interface IFirestore : IFirebase {

    companion object {

        fun metadataCollection(): String {
            return "metadata"
        }
    }
}