package com.virtualsoft.firebase.services.firestore

import android.content.Context
import com.virtualsoft.core.service.database.ITreeDatabase
import com.virtualsoft.core.utils.AppUtils.getEnvironment
import com.virtualsoft.firebase.IFirebase

interface IFirestore : IFirebase, ITreeDatabase {

    var context: Context?

    companion object {

        //COLLECTIONS
        fun environmentsCollection(): String {
            return "environments"
        }

        fun metadataCollection(context: Context?): String {
            return "${context?.getEnvironment()}_metadata"
        }

        fun treedataCollection(context: Context?): String {
            return "${context?.getEnvironment()}_treedata"
        }

        fun getParentPath(documentPath: String): String {
            return documentPath.split("/").dropLast(1).joinToString("/")
        }

        fun getChildsPath(context: Context?, documentPath: String): String {
            return "$documentPath/${treedataCollection(context)}"
        }
    }
}