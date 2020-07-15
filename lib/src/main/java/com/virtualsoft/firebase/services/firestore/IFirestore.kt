package com.virtualsoft.firebase.services.firestore

import android.content.Context
import com.virtualsoft.core.service.database.ITreeDatabase
import com.virtualsoft.core.utils.AppUtils.getEnvironment
import com.virtualsoft.firebase.IFirebase

interface IFirestore : IFirebase {

    var context: Context?

    companion object {

        fun metadataCollection(): String {
            return "metadata"
        }
    }
}