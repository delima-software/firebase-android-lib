package com.virtualsoft.firebase.services.firestore.database

import android.content.Context
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.virtualsoft.core.designpatterns.builder.IBuilder
import com.virtualsoft.core.service.database.IFirestoreDatabase
import com.virtualsoft.core.service.database.data.IDocument
import com.virtualsoft.core.utils.DateUtils
import com.virtualsoft.core.utils.DateUtils.isBeforeDateTime
import com.virtualsoft.firebase.data.database.Metadata
import com.virtualsoft.firebase.data.database.IMetadata
import com.virtualsoft.firebase.services.firestore.FirestorePreferences
import com.virtualsoft.firebase.services.firestore.IFirestore
import com.virtualsoft.firebase.utils.LogUtils

class FirestoreDatabase(override var context: Context? = null) :
    IFirestore, IFirestoreDatabase {

    data class Properties(var dataTypeResolver: ((String) -> Class<out IDocument>)? = null)

    private var metadataSnapshotMap = hashMapOf<String, DocumentSnapshot>()
    private var firestoreDatabaseProperties: Properties? = null

    override val id: String
        get() = FirestoreDatabase::class.java.name

    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    class Builder(context: Context?) : IBuilder<IFirestore> {

        override val building =
            FirestoreDatabase(
                context
            )

        fun setFirestoreDatabaseProperties(firestoreDatabaseProperties: Properties?): Builder {
            building.firestoreDatabaseProperties = firestoreDatabaseProperties
            return this
        }
    }

    private fun addMetadataSnapshotListener(metadataId: String) {
        firestore.collection(IFirestore.metadataCollection()).document(metadataId).addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            when {
                firebaseFirestoreException != null -> {
                    LogUtils.logError("READ_METADATA", "cannot read metadata from firestore")
                }
                documentSnapshot?.exists() == true -> {
                    metadataSnapshotMap[metadataId] = documentSnapshot
                    LogUtils.logSuccess("READ_METADATA", "read metadata from firestore success")
                }
                else -> {
                    LogUtils.logError("READ_METADATA", "cannot read metadata from firestore")
                }
            }
        }
    }

    private fun readMetadata(metadataId: String, callback: (IMetadata?) -> Unit) {
        val metadataSnapshot = metadataSnapshotMap[metadataId]
        when {
            metadataSnapshot == null -> {
                addMetadataSnapshotListener(metadataId)
                firestore.collection(IFirestore.metadataCollection()).document(metadataId).get()
                    .addOnSuccessListener { documentSnapshot ->
                        val metadata = documentSnapshot.toObject<Metadata>()
                        LogUtils.logSuccess("READ_METADATA", "read metadata from firestore success")
                        callback(metadata)
                    }
                    .addOnFailureListener {
                        LogUtils.logError("READ_METADATA", "cannot read metadata from firestore")
                        callback(null)
                    }
            }
            metadataSnapshot.exists() -> {
                val metadata = metadataSnapshot.toObject<Metadata>()
                LogUtils.logSuccess("READ_METADATA", "read metadata from firestore success")
                callback(metadata)
            }
            else -> {
                LogUtils.logError("READ_METADATA", "cannot read metadata from firestore")
                callback(null)
            }
        }
    }

    private fun writeMetadata(metadataId: String, metadata: IMetadata, callback: ((Boolean) -> Unit)? = null) {
        firestore.collection(IFirestore.metadataCollection()).document(metadataId).set(metadata)
            .addOnSuccessListener {
                LogUtils.logSuccess("WRITE_METADATA", "write metadata to firestore success")
                callback?.invoke(true)
            }
            .addOnFailureListener {
                LogUtils.logError("WRITE_METADATA", "cannot write metadata to firestore")
                callback?.invoke(false)
            }
    }

    private fun updateMetadata(metadataId: String, field: String, value: Any, callback: ((Boolean) -> Unit)? = null) {
        firestore.collection(IFirestore.metadataCollection()).document(metadataId).update(field, value)
            .addOnSuccessListener {
                LogUtils.logSuccess("UPDATE_METADATA", "update metadata to firestore success")
                callback?.invoke(true)
            }
            .addOnFailureListener {
                LogUtils.logError("UPDATE_METADATA", "cannot update metadata to firestore")
                callback?.invoke(false)
            }
    }

    private fun deleteMetadata(metadataId: String, callback: ((Boolean) -> Unit)? = null) {
        firestore.collection(IFirestore.metadataCollection()).document(metadataId).delete()
            .addOnSuccessListener {
                LogUtils.logSuccess("DELETE_METADATA", "delete metadata from firestore success")
                callback?.invoke(true)
            }
            .addOnFailureListener {
                LogUtils.logError("DELETE_METADATA", "cannot delete metadata from firestore")
                callback?.invoke(false)
            }
    }

    override fun readDocument(documentReference: DocumentReference, callback: (IDocument?) -> Unit) {
        val metadataId = documentReference.path
        readMetadata(metadataId) { metadata ->
            var source = Source.DEFAULT
            val lastRead = FirestorePreferences.getLastRead(metadataId, context)
            val lastUpdate = metadata?.lastUpdate
            if (lastRead != null && lastUpdate?.isBeforeDateTime(lastRead) == true)
                source = Source.CACHE
            documentReference.get(source)
                .addOnSuccessListener { document ->
                    FirestorePreferences.setLastRead(metadataId, DateUtils.currentDate(), context)
                    val classType = firestoreDatabaseProperties?.dataTypeResolver?.invoke(document.get("type").toString())
                    if (classType != null) {
                        val data = document.toObject(classType)
                        if (data != null)
                            callback(data)
                        else {
                            LogUtils.logError(
                                "READ_DATA",
                                "cannot read data with the specified id"
                            )
                            callback(null)
                        }
                    }
                    else {
                        LogUtils.logError(
                            "READ_DATA",
                            "cannot read data with the specified id"
                        )
                        callback(null)
                    }
                }
                .addOnFailureListener {
                    LogUtils.logError(
                        "READ_DATA",
                        "cannot read data at the specified path",
                        it
                    )
                    callback(null)
                }
        }
    }

    override fun readCollection(collectionReference: CollectionReference, callback: (List<IDocument>) -> Unit) {
        val metadataId = collectionReference.path
        readMetadata(metadataId) { metadata ->
            var source = Source.DEFAULT
            val lastRead = FirestorePreferences.getLastRead(metadataId, context)
            val lastUpdate = metadata?.lastUpdate
            if (lastRead != null && lastUpdate?.isBeforeDateTime(lastRead) == true)
                source = Source.CACHE
            collectionReference.get(source)
                .addOnSuccessListener { documents ->
                    FirestorePreferences.setLastRead(
                        metadataId,
                        DateUtils.currentDate(),
                        context
                    )
                    val list = mutableListOf<IDocument>()
                    for (document in documents) {
                        val classType = firestoreDatabaseProperties?.dataTypeResolver?.invoke(document.get("type").toString())
                        if (classType != null)
                            list.add(document.toObject(classType))
                    }
                    LogUtils.logSuccess(
                        "READ_DATA",
                        "read data at the specified path success"
                    )
                    callback(list)
                }
                .addOnFailureListener {
                    LogUtils.logError(
                        "READ_DATA",
                        "cannot read data at the specified path",
                        it
                    )
                    callback(listOf())
                }
        }
    }

    override fun readCollection(collectionId: String, query: Query, callback: (List<IDocument>) -> Unit) {
        readMetadata(collectionId) { metadata ->
            var source = Source.DEFAULT
            val lastRead = FirestorePreferences.getLastRead(collectionId, context)
            val lastUpdate = metadata?.lastUpdate
            if (lastRead != null && lastUpdate?.isBeforeDateTime(lastRead) == true)
                source = Source.CACHE
            query.get(source)
                .addOnSuccessListener { documents ->
                    FirestorePreferences.setLastRead(
                        collectionId,
                        DateUtils.currentDate(),
                        context
                    )
                    val list = mutableListOf<IDocument>()
                    for (document in documents) {
                        val classType = firestoreDatabaseProperties?.dataTypeResolver?.invoke(document.get("type").toString())
                        if (classType != null)
                            list.add(document.toObject(classType))
                    }
                    LogUtils.logSuccess(
                        "READ_DATA",
                        "read data at the specified path success"
                    )
                    callback(list)
                }
                .addOnFailureListener {
                    LogUtils.logError(
                        "READ_DATA",
                        "cannot read data at the specified path",
                        it
                    )
                    callback(listOf())
                }
        }
    }

    override fun writeDocument(documentReference: DocumentReference, data: IDocument, callback: ((Boolean) -> Unit)?) {
        documentReference.set(data)
            .addOnSuccessListener {
                val documentMetadataId = documentReference.path
                val collectionMetadataId = documentReference.parent.path
                writeMetadata(documentMetadataId, Metadata.buildMetadata(documentMetadataId, context))
                writeMetadata(collectionMetadataId, Metadata.buildMetadata(collectionMetadataId, context))
                LogUtils.logSuccess(
                    "WRITE_DATA",
                    "write data at the specified path success"
                )
                callback?.invoke(true)
            }
            .addOnFailureListener {
                LogUtils.logError(
                    "WRITE_DATA",
                    "cannot write data at the specified path",
                    it
                )
                callback?.invoke(false)
            }
    }

    override fun updateDocument(documentReference: DocumentReference, field: String, value: Any, callback: ((Boolean) -> Unit)?) {
        documentReference.update(field, value)
            .addOnSuccessListener {
                val documentMetadataId = documentReference.path
                val collectionMetadataId = documentReference.parent.path
                writeMetadata(documentMetadataId, Metadata.buildMetadata(documentMetadataId, context))
                writeMetadata(collectionMetadataId, Metadata.buildMetadata(collectionMetadataId, context))
                LogUtils.logSuccess(
                    "UPDATE_DATA",
                    "update data at the specified path success"
                )
                callback?.invoke(true)
            }
            .addOnFailureListener {
                LogUtils.logError(
                    "UPDATE_DATA",
                    "cannot update data at the specified path",
                    it
                )
                callback?.invoke(false)
            }
    }

    override fun deleteDocument(documentReference: DocumentReference, callback: ((Boolean) -> Unit)?) {
        documentReference.delete()
            .addOnSuccessListener {
                val documentMetadataId = documentReference.path
                val collectionMetadataId = documentReference.parent.path
                deleteMetadata(documentMetadataId)
                writeMetadata(collectionMetadataId, Metadata.buildMetadata(collectionMetadataId, context))
                LogUtils.logSuccess(
                    "DELETE_DATA",
                    "delete data at the specified path success"
                )
                callback?.invoke(true)
            }
            .addOnFailureListener {
                LogUtils.logError(
                    "DELETE_DATA",
                    "cannot delete data at the specified path",
                    it
                )
                callback?.invoke(false)
            }
    }
}