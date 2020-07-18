package com.virtualsoft.firebase.services.firestore.database

import android.content.Context
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.virtualsoft.core.designpatterns.builder.IBuilder
import com.virtualsoft.core.utils.DateUtils.currentDate
import com.virtualsoft.core.utils.DateUtils.isBeforeDateTime
import com.virtualsoft.core.utils.GeneratorUtils.generateUUID
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

    private enum class COLLECTIONS {
        metadata
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

    private fun getLastMetadataIndex(collection: List<Metadata>): Int {
        var i = 0
        collection.forEachIndexed { index, document ->
            val metadataLastUpdate = collection[i].lastUpdate
            val documentLastUpdate = document.lastUpdate
            if (metadataLastUpdate != null && documentLastUpdate != null) {
                if (metadataLastUpdate.isBeforeDateTime(documentLastUpdate))
                    i = index
            }
        }
        return i
    }

    private fun addMetadataSnapshotListener(metadataName: String) {
        firestore.collection(COLLECTIONS.metadata.name).whereEqualTo("name", metadataName).addSnapshotListener { collectionSnapshot, firebaseFirestoreException ->
            when {
                firebaseFirestoreException != null -> {
                    LogUtils.logError("READ_METADATA", "cannot read metadata from firestore: $metadataName", firebaseFirestoreException)
                }
                collectionSnapshot?.isEmpty == false -> {
                    val index = getLastMetadataIndex(collectionSnapshot.toObjects())
                    metadataSnapshotMap[metadataName] = collectionSnapshot.elementAt(index)
                    LogUtils.logSuccess("READ_METADATA", "read metadata from firestore success")
                }
                else -> {
                    LogUtils.logError("READ_METADATA", "cannot read metadata from firestore: $metadataName")
                }
            }
        }
    }

    private suspend fun readMetadata(metadataName: String): IMetadata? {
        val metadataSnapshot = metadataSnapshotMap[metadataName]
        when {
            metadataSnapshot == null -> {
                addMetadataSnapshotListener(metadataName)
                return try {
                    val collection = firestore.collection(COLLECTIONS.metadata.name).whereEqualTo("name", metadataName).get().await()
                    if (collection.isEmpty) {
                        LogUtils.logError("READ_METADATA", "metadata does not exists: $metadataName")
                        null
                    }
                    else {
                        LogUtils.logSuccess("READ_METADATA", "read metadata from firestore success")
                        val index = getLastMetadataIndex(collection.toObjects())
                        val metadata = collection.elementAt(index).toObject<Metadata>()
                        metadata
                    }
                }
                catch (e: Exception) {
                    LogUtils.logError("READ_METADATA", "cannot read metadata from firestore: $metadataName", e)
                    null
                }
            }
            metadataSnapshot.exists() -> {
                val metadata = metadataSnapshot.toObject<Metadata>()
                LogUtils.logSuccess("READ_METADATA", "read metadata from firestore success")
                return metadata
            }
            else -> {
                LogUtils.logError("READ_METADATA", "cannot read metadata from firestore: $metadataName")
                return null
            }
        }
    }

    private suspend fun writeMetadata(metadataName: String): Boolean {
        return try {
            var metadata = readMetadata(metadataName)
            if (metadata == null)
                metadata = Metadata.buildMetadata(metadataName, context)
            firestore.collection(COLLECTIONS.metadata.name).document(metadata.id!!).set(metadata).await()
            LogUtils.logSuccess("WRITE_METADATA", "write metadata to firestore success")
            true
        }
        catch (e: Exception) {
            LogUtils.logError("WRITE_METADATA", "cannot write metadata to firestore: $metadataName", e)
            false
        }
    }

    private suspend fun updateMetadata(metadataName: String, field: String, value: Any): Boolean {
        return try {
            val metadataStored = readMetadata(metadataName)
            val metadataId = metadataStored?.id
            if (metadataId != null) {
                firestore.collection(COLLECTIONS.metadata.name).document(metadataId).update(field, value).await()
                LogUtils.logSuccess("UPDATE_METADATA", "update metadata to firestore success")
                true
            }
            else {
                LogUtils.logError("UPDATE_METADATA", "cannot update metadata to firestore: $metadataName")
                false
            }
        }
        catch (e: Exception) {
            LogUtils.logError("UPDATE_METADATA", "cannot update metadata to firestore: $metadataName", e)
            false
        }
    }

    private suspend fun deleteMetadata(metadataName: String): Boolean {
        return try {
            val metadataStored = readMetadata(metadataName)
            val metadataId = metadataStored?.id
            if (metadataId != null) {
                firestore.collection(COLLECTIONS.metadata.name).document(metadataId).delete().await()
                LogUtils.logSuccess("DELETE_METADATA", "delete metadata from firestore success")
                true
            }
            else {
                LogUtils.logError("DELETE_METADATA", "cannot delete metadata from firestore: $metadataName")
                false
            }
        }
        catch (e: Exception) {
            LogUtils.logError("DELETE_METADATA", "cannot delete metadata from firestore: $metadataName", e)
            false
        }
    }

    override suspend fun readDocument(documentReference: DocumentReference): IDocument? {
        val metadataName = documentReference.path
        val metadata = readMetadata(metadataName)
        var source = Source.DEFAULT
        val lastRead = FirestorePreferences.getLastRead(metadataName, context)
        val lastUpdate = metadata?.lastUpdate
        if (lastRead != null && lastUpdate?.isBeforeDateTime(lastRead) == true)
            source = Source.CACHE
        return try {
            val document = documentReference.get(source).await()
            FirestorePreferences.setLastRead(metadataName, currentDate(), context)
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
        val metadataName = collectionReference.path
        val metadata = readMetadata(metadataName)
        var source = Source.DEFAULT
        val lastRead = FirestorePreferences.getLastRead(metadataName, context)
        val lastUpdate = metadata?.lastUpdate
        if (lastRead != null && lastUpdate?.isBeforeDateTime(lastRead) == true)
            source = Source.CACHE
        return try {
            val collection = collectionReference.get(source).await()
            FirestorePreferences.setLastRead(metadataName, currentDate(), context)
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
        val metadataName = collectionReference.path
        val metadata = readMetadata(metadataName)
        var source = Source.DEFAULT
        val lastRead = FirestorePreferences.getLastRead(metadataName, context)
        val lastUpdate = metadata?.lastUpdate
        if (lastRead != null && lastUpdate?.isBeforeDateTime(lastRead) == true)
            source = Source.CACHE
        return try {
            val collection = query.get(source).await()
            FirestorePreferences.setLastRead(metadataName, currentDate(), context)
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
            documentReference.set(data).await()
            val documentMetadataName = documentReference.path
            val collectionMetadataName = documentReference.parent.path
            writeMetadata(documentMetadataName)
            writeMetadata(collectionMetadataName)
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
            documentReference.update(field, value).await()
            val documentMetadataName = documentReference.path
            val collectionMetadataName = documentReference.parent.path
            writeMetadata(documentMetadataName)
            writeMetadata(collectionMetadataName)
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
            val documentMetadataName = documentReference.path
            val collectionMetadataName = documentReference.parent.path
            deleteMetadata(documentMetadataName)
            writeMetadata(collectionMetadataName)
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