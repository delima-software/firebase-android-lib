package com.virtualsoft.firebase.services.firestore

import android.content.Context
import com.virtualsoft.core.service.database.ITreeDatabase
import com.virtualsoft.core.utils.AppUtils.getEnvironment
import com.virtualsoft.firebase.IFirebase

interface IFirestore : IFirebase, ITreeDatabase {

    var context: Context?

    companion object {

        //COLLECTIONS
        fun metadataCollection(): String {
            return "metadata"
        }

        fun treedataCollection(): String {
            return "treedata"
        }

        fun getParentPath(documentPath: String): String {
            return documentPath.split("/").dropLast(1).joinToString("/")
        }

        fun getChildsPath(documentPath: String): String {
            return "$documentPath/${treedataCollection()}"
        }
    }
}