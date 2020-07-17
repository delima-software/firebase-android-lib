package com.virtualsoft.firebase.services.firestore.database

import android.content.Context
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.virtualsoft.core.designpatterns.builder.IBuilder
import com.virtualsoft.core.utils.DateUtils.currentDate
import com.virtualsoft.core.utils.DateUtils.isBeforeDateTime
import com.virtualsoft.firebase.data.database.Metadata
import com.virtualsoft.firebase.data.database.IMetadata
import com.virtualsoft.firebase.services.firestore.FirestorePreferences
import com.virtualsoft.firebase.services.firestore.IFirestore
import com.virtualsoft.firebase.utils.LogUtils
import kotlinx.coroutines.tasks.await

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

    private suspend fun readMetadata(metadataId: String): IMetadata? {
        val metadataSnapshot = metadataSnapshotMap[metadataId]
        when {
            metadataSnapshot == null -> {
                addMetadataSnapshotListener(metadataId)
                return try {
                    val documentSnapshot = firestore.collection(IFirestore.metadataCollection()).document(metadataId).get().await()
                    val metadata = documentSnapshot.toObject<Metadata>()
                    LogUtils.logSuccess("READ_METADATA", "read metadata from firestore success")
                    metadata
                }
                catch (e: Exception) {
                    LogUtils.logError("READ_METADATA", "cannot read metadata from firestore", e)
                    null
                }
            }
            metadataSnapshot.exists() -> {
                val metadata = metadataSnapshot.toObject<Metadata>()
                LogUtils.logSuccess("READ_METADATA", "read metadata from firestore success")
                return metadata
            }
            else -> {
                LogUtils.logError("READ_METADATA", "cannot read metadata from firestore")
                return null
            }
        }
    }

    private suspend fun writeMetadata(metadataId: String, metadata: IMetadata): Boolean {
        return try {
            firestore.collection(IFirestore.metadataCollection()).document(metadataId).set(metadata).await()
            LogUtils.logSuccess("WRITE_METADATA", "write metadata to firestore success")
            true
        }
        catch (e: Exception) {
            LogUtils.logError("WRITE_METADATA", "cannot write metadata to firestore", e)
            false
        }
    }

    private suspend fun updateMetadata(metadataId: String, field: String, value: Any): Boolean {
        return try {
            firestore.collection(IFirestore.metadataCollection()).document(metadataId).update(field, value).await()
            LogUtils.logSuccess("UPDATE_METADATA", "update metadata to firestore success")
            true
        }
        catch (e: Exception) {
            LogUtils.logError("UPDATE_METADATA", "cannot update metadata to firestore", e)
            false
        }
    }

    private suspend fun deleteMetadata(metadataId: String): Boolean {
        return try {
            firestore.collection(IFirestore.metadataCollection()).document(metadataId).delete().await()
            LogUtils.logSuccess("DELETE_METADATA", "delete metadata from firestore success")
            true
        }
        catch (e: Exception) {
            LogUtils.logError("DELETE_METADATA", "cannot delete metadata from firestore", e)
            false
        }
    }

    override suspend fun readDocument(documentReference: DocumentReference): IDocument? {
        val metadataId = documentReference.path
        val metadata = readMetadata(metadataId)
        var source = Source.DEFAULT
        val lastRead = FirestorePreferences.getLastRead(metadataId, context)
        val lastUpdate = metadata?.lastUpdate
        if (lastRead != null && lastUpdate?.isBeforeDateTime(lastRead) == true)
            source = Source.CACHE
        return try {
            val document = documentReference.get(source).await()
            FirestorePreferences.setLastRead(metadataId, currentDate(), context)
            val classType = firestoreDatabaseProperties?.dataTypeResolver?.invoke(document.get("type").toString())
            document.toObject(classType!!)
        }
        catch (e: Exception) {
            LogUtils.logError(
                "READ_DATA",
                "cannot read data with the specified id",
                e
            )
            null
        }
    }

    override suspend fun readCollection(collectionReference: CollectionReference): List<IDocument> {
        val metadataId = collectionReference.path
        val metadata = readMetadata(metadataId)
        var source = Source.DEFAULT
        val lastRead = FirestorePreferences.getLastRead(metadataId, context)
        val lastUpdate = metadata?.lastUpdate
        if (lastRead != null && lastUpdate?.isBeforeDateTime(lastRead) == true)
            source = Source.CACHE
        return try {
            val collection = collectionReference.get(source).await()
            FirestorePreferences.setLastRead(metadataId, currentDate(), context)
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
                "cannot read data at the specified path",
                e
            )
            listOf()
        }
    }

    override suspend fun readCollection(collectionId: String, query: Query): List<IDocument> {
        val metadata = readMetadata(collectionId)
        var source = Source.DEFAULT
        val lastRead = FirestorePreferences.getLastRead(collectionId, context)
        val lastUpdate = metadata?.lastUpdate
        if (lastRead != null && lastUpdate?.isBeforeDateTime(lastRead) == true)
            source = Source.CACHE
        return try {
            val collection = query.get(source).await()
            FirestorePreferences.setLastRead(collectionId, currentDate(), context)
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
                "cannot read data at the specified path",
                e
            )
            listOf()
        }
    }

    override suspend fun writeDocument(documentReference: DocumentReference, data: IDocument): Boolean {
        return try {
            documentReference.set(data).await()
            val documentMetadataId = documentReference.path
            val collectionMetadataId = documentReference.parent.path
            writeMetadata(documentMetadataId, Metadata.buildMetadata(documentMetadataId, context))
            writeMetadata(collectionMetadataId, Metadata.buildMetadata(collectionMetadataId, context))
            true
        }
        catch (e: Exception) {
            LogUtils.logError(
                "WRITE_DATA",
                "cannot write data at the specified path",
                e
            )
            false
        }
    }

    override suspend fun updateDocument(documentReference: DocumentReference, field: String, value: Any): Boolean {
        return try {
            documentReference.update(field, value).await()
            val documentMetadataId = documentReference.path
            val collectionMetadataId = documentReference.parent.path
            writeMetadata(documentMetadataId, Metadata.buildMetadata(documentMetadataId, context))
            writeMetadata(collectionMetadataId, Metadata.buildMetadata(collectionMetadataId, context))
            LogUtils.logSuccess(
                "UPDATE_DATA",
                "update data at the specified path success"
            )
            true
        }
        catch (e: Exception) {
            LogUtils.logError(
                "UPDATE_DATA",
                "cannot update data at the specified path",
                e
            )
            false
        }
    }

    override suspend fun deleteDocument(documentReference: DocumentReference): Boolean {
        return try {
            documentReference.delete().await()
            val documentMetadataId = documentReference.path
            val collectionMetadataId = documentReference.parent.path
            deleteMetadata(documentMetadataId)
            writeMetadata(collectionMetadataId, Metadata.buildMetadata(collectionMetadataId, context))
            LogUtils.logSuccess(
                "DELETE_DATA",
                "delete data at the specified path success"
            )
            true
        }
        catch (e: Exception) {
            LogUtils.logError(
                "DELETE_DATA",
                "cannot delete data at the specified path",
                e
            )
            false
        }
    }
}