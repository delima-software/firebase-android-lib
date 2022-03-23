package com.virtualsoft.firebase.services.firestore.database

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.virtualsoft.core.designpatterns.builder.IBuilder
import com.virtualsoft.core.utils.DateUtils.currentDate
import com.virtualsoft.core.utils.DateUtils.isBeforeDateTime
import com.virtualsoft.firebase.data.database.Metadata
import com.virtualsoft.firebase.data.database.IMetadata
import com.virtualsoft.firebase.services.firestore.FirestorePreferences
import com.virtualsoft.firebase.services.firestore.IFirestore
import com.virtualsoft.firebase.utils.LogUtils
import kotlinx.coroutines.tasks.await

class FirestoreDatabase : IFirestore, IFirestoreDatabase {

    data class Properties(var dataTypeResolver: ((String) -> Class<out IDocument>)? = null)

    private var firestoreDatabaseProperties: Properties? = null

    override val id: String
        get() = FirestoreDatabase::class.java.name

    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    class Builder : IBuilder<IFirestore> {

        override val building = FirestoreDatabase()

        fun setFirestoreDatabaseProperties(firestoreDatabaseProperties: Properties?): Builder {
            building.firestoreDatabaseProperties = firestoreDatabaseProperties
            return this
        }
    }

    override suspend fun readDocument(documentReference: DocumentReference): IDocument? {
        var source = Source.DEFAULT
        return try {
            val document = documentReference.get(source).await()
            val classType = firestoreDatabaseProperties?.dataTypeResolver?.invoke(document.get("type").toString())
            document.toObject(classType!!)
        }
        catch (e: Exception) {
            LogUtils.logError(
                "READ_DATA",
                "cannot read data at the specified path: ${documentReference.path}",
                e
            )
            null
        }
    }

    override suspend fun readCollection(collectionReference: CollectionReference): List<IDocument> {
        var source = Source.DEFAULT
        return try {
            val collection = collectionReference.get(source).await()
            val list = mutableListOf<IDocument>()
            for (document in collection) {
                val classType = firestoreDatabaseProperties?.dataTypeResolver?.invoke(document.get("type").toString())
                list.add(document.toObject(classType!!))
            }
            list
        }
        catch (e: Exception) {
            LogUtils.logError(
                "READ_DATA",
                "cannot read data at the specified path: ${collectionReference.path}",
                e
            )
            listOf()
        }
    }

    override suspend fun readCollection(collectionReference: CollectionReference, query: Query): List<IDocument> {
        var source = Source.DEFAULT
        return try {
            val collection = query.get(source).await()
            val list = mutableListOf<IDocument>()
            for (document in collection) {
                val classType = firestoreDatabaseProperties?.dataTypeResolver?.invoke(document.get("type").toString())
                list.add(document.toObject(classType!!))
            }
            list
        }
        catch (e: Exception) {
            LogUtils.logError(
                "READ_DATA",
                "cannot read data at the specified path: ${collectionReference.path}",
                e
            )
            listOf()
        }
    }

    override suspend fun writeDocument(documentReference: DocumentReference, data: IDocument): Boolean {
        return try {
            val currentDate = currentDate()
            data.lastUpdate = currentDate
            documentReference.set(data).await()
            true
        }
        catch (e: Exception) {
            LogUtils.logError(
                "WRITE_DATA",
                "cannot write data at the specified path: ${documentReference.path}",
                e
            )
            false
        }
    }

    override suspend fun updateDocument(documentReference: DocumentReference, field: String, value: Any): Boolean {
        return try {
            val currentDate = currentDate()
            documentReference.update(field, value).await()
            if (field != IDocument::lastUpdate.name)
                documentReference.update(IDocument::lastUpdate.name, currentDate).await()
            true
        }
        catch (e: Exception) {
            LogUtils.logError(
                "UPDATE_DATA",
                "cannot update data at the specified path: ${documentReference.path}",
                e
            )
            false
        }
    }

    override suspend fun deleteDocument(documentReference: DocumentReference): Boolean {
        return try {
            documentReference.delete().await()
            true
        }
        catch (e: Exception) {
            LogUtils.logError(
                "DELETE_DATA",
                "cannot delete data at the specified path: ${documentReference.path}",
                e
            )
            false
        }
    }
}