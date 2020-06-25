package com.virtualsoft.firebase.services.firestore

import android.content.Context
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.virtualsoft.core.service.database.data.ITreeData
import com.virtualsoft.core.designpatterns.builder.IBuilder
import com.virtualsoft.core.utils.DateUtils.currentDate
import com.virtualsoft.core.utils.DateUtils.isBeforeDateTime
import com.virtualsoft.core.utils.TextUtils.nextAlphabeticString
import com.virtualsoft.firebase.data.IMetadata
import com.virtualsoft.firebase.data.Metadata
import com.virtualsoft.firebase.utils.LogUtils.logError
import com.virtualsoft.firebase.utils.LogUtils.logSuccess

class Firestore(override var context: Context? = null) :
    IFirestore {

    data class Properties(var dataTypeResolver: ((String) -> Class<out ITreeData>)? = null)

    private var metadataSnapshot: DocumentSnapshot? = null
    private var firestoreProperties: Properties? = null

    override val id: String
        get() = Firestore::class.java.name

    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    class Builder(context: Context?) : IBuilder<IFirestore> {

        override val building =
            Firestore(context)

        fun setFirestoreProperties(firestoreProperties: Properties?): Builder {
            building.firestoreProperties = firestoreProperties
            return this
        }
    }

    private fun writeMetadata(documentId: String, metadata: IMetadata, callback: ((Boolean) -> Unit)? = null) {
        getMetadataCollection()?.document(documentId)?.set(metadata)
            ?.addOnSuccessListener {
                logSuccess("WRITE_METADATA", "write metadata to firestore success")
                callback?.invoke(true)
            }
            ?.addOnFailureListener {
                logError("WRITE_METADATA", "cannot write metadata to firestore")
                callback?.invoke(false)
            }
    }

    private fun addMetadataSnapshotListener(documentId: String) {
        getMetadataCollection()?.document(documentId)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null)
                logError("READ_METADATA", "cannot read metadata from firestore")
            else if (documentSnapshot?.exists() == true) {
                metadataSnapshot = documentSnapshot
                logSuccess("READ_METADATA", "read metadata from firestore success")
            }
            else
                logError("READ_METADATA", "cannot read metadata from firestore")
        }
    }

    private fun readMetadata(documentId: String, callback: (IMetadata?) -> Unit) {
        if (metadataSnapshot == null) {
            addMetadataSnapshotListener(documentId)
            getMetadataCollection()?.document(documentId)?.get()
                ?.addOnSuccessListener { documentSnapshot ->
                    val metadata = documentSnapshot.toObject<Metadata>()
                    logSuccess("READ_METADATA", "read metadata from firestore success")
                    callback(metadata)
                }
                ?.addOnFailureListener {
                    logError("READ_METADATA", "cannot read metadata from firestore")
                    callback(null)
                }
        }
        else if (metadataSnapshot?.exists() == true) {
            val metadata = metadataSnapshot!!.toObject<Metadata>()
            logSuccess("READ_METADATA", "read metadata from firestore success")
            callback(metadata)
        }
        else {
            logError("READ_METADATA", "cannot read metadata from firestore")
            callback(null)
        }
    }

    private fun deleteMetadata(documentId: String, callback: ((Boolean) -> Unit)? = null) {
        getMetadataCollection()?.document(documentId)?.delete()
            ?.addOnSuccessListener {
                logSuccess("DELETE_METADATA", "delete metadata from firestore success")
                callback?.invoke(true)
            }
            ?.addOnFailureListener {
                logError("DELETE_METADATA", "cannot delete metadata from firestore")
                callback?.invoke(false)
            }
    }

    private fun updateMetadata(documentId: String, field: String, value: Any, callback: ((Boolean) -> Unit)? = null) {
        getMetadataCollection()?.document(documentId)?.update(field, value)
            ?.addOnSuccessListener {
                logSuccess("UPDATE_METADATA", "update metadata to firestore success")
                callback?.invoke(true)
            }
            ?.addOnFailureListener {
                logError("UPDATE_METADATA", "cannot update metadata to firestore")
                callback?.invoke(false)
            }
    }

    private fun updateMapMetadata(documentId: String, path: String, type: String? = null, delete: Boolean? = null) {
        readMetadata(documentId) { metadata ->
            metadata?.updateMap?.let { updateMap ->
                val currentDate = currentDate()
                type?.let {
                    updateMap[allType(it)] = currentDate
                }
                if (delete == true)
                    updateMap.remove(path)
                else
                    updateMap[path] = currentDate
                updateMap[IFirestore.getParentPath(path)] = currentDate
                updateMetadata(documentId, IMetadata::updateMap.name, updateMap)
            }
        }
    }

    override fun readTreeData(path: String, callback: (ITreeData?) -> Unit) {
        val metadataId = path.split("/")[1]
        readMetadata(metadataId) { metadata ->
            var source = Source.DEFAULT
            val lastRead = FirestorePreferences.getLastRead(path, context)
            val lastUpdate = metadata?.updateMap?.get(path)
            if (lastRead != null && lastUpdate?.isBeforeDateTime(lastRead) == true)
                source = Source.CACHE
            getDocument(path)?.get(source)
                ?.addOnSuccessListener { document ->
                    FirestorePreferences.setLastRead(path, currentDate(), context)
                    val classType = firestoreProperties?.dataTypeResolver?.invoke(document.get("type").toString())
                    if (classType != null) {
                        val data = document.toObject(classType)
                        if (data != null)
                            callback(data)
                        else {
                            logError("READ_TREE_DATA", "cannot read data with the specified id")
                            callback(null)
                        }
                    }
                    else {
                        logError("READ_TREE_DATA", "cannot read data with the specified id")
                        callback(null)
                    }
                }
                ?.addOnFailureListener {
                    logError("READ_TREE_DATA", "cannot read tree data at the specified path", it)
                    callback(null)
                }
        }
    }

    override fun readTreeDataChilds(path: String, callback: (List<ITreeData>) -> Unit) {
        val metadataId = path.split("/")[1]
        readMetadata(metadataId) { metadata ->
            val childsPath = IFirestore.getChildsPath(path)
            var source = Source.DEFAULT
            val lastRead = FirestorePreferences.getLastRead(childsPath, context)
            val lastUpdate = metadata?.updateMap?.get(childsPath)
            if (lastRead != null && lastUpdate?.isBeforeDateTime(lastRead) == true)
                source = Source.CACHE
            getCollection(childsPath)?.get(source)
                ?.addOnSuccessListener { documents ->
                    FirestorePreferences.setLastRead(childsPath, currentDate(), context)
                    val list = mutableListOf<ITreeData>()
                    for (document in documents) {
                        val classType = firestoreProperties?.dataTypeResolver?.invoke(document.get("type").toString())
                        if (classType != null)
                            list.add(document.toObject(classType))
                    }
                    logSuccess("READ_TREE_DATA", "read tree data at the specified path success")
                    callback(list)
                }
                ?.addOnFailureListener {
                    logError("READ_TREE_DATA", "cannot read tree data at the specified path", it)
                    callback(listOf())
                }
        }
    }

    override fun writeTreeData(path: String, data: ITreeData, callback: ((Boolean) -> Unit)?) {
        if (path == data.completePath()) {
            getDocument(path)?.set(data)
                ?.addOnSuccessListener {
                    val tokens = path.split("/")
                    val metatadaId = tokens[1]
                    if (tokens.size <= 2)
                        writeMetadata(metatadaId, IMetadata.buildMetadata(metatadaId, context))
                    else
                        updateMapMetadata(metatadaId, path, type = data.type)
                    logSuccess("WRITE_TREE_DATA", "write tree data at the specified path success")
                    callback?.invoke(true)
                }
                ?.addOnFailureListener {
                    logError("WRITE_TREE_DATA", "cannot write tree data at the specified path", it)
                    callback?.invoke(false)
                }
        }
        else {
            logError("WRITE_TREE_DATA", "cannot write tree data at the specified path")
            callback?.invoke(false)
        }
    }

    override fun writeTreeDataChilds(path: String, childs: List<ITreeData>, callback: ((Boolean) -> Unit)?) {
        var completed = 0
        for (child in childs) {
            child.path = IFirestore.getChildsPath(path)
            writeTreeData(child.completePath(), child) { wrote ->
                if (wrote)
                    completed++
                else {
                    logError("WRITE_TREE_DATA_CHILDS", "cannot write tree data at the specified path")
                    callback?.invoke(false)
                }

                if(completed == childs.size) {
                    logSuccess("WRITE_TREE_DATA_CHILDS", "write tree data at the specified path success")
                    callback?.invoke(true)
                }
            }
        }
    }

    override fun updateTreeData(path: String, field: String, value: Any, callback: ((Boolean) -> Unit)?) {
        readTreeData(path) { treeData ->
            getDocument(path)?.update(field, value)
                ?.addOnSuccessListener {
                    val metadataId = path.split("/")[1]
                    updateMapMetadata(metadataId, path, type = treeData?.type)
                    logSuccess("UPDATE_TREE_DATA", "update tree data at the specified path success")
                    callback?.invoke(true)
                }
                ?.addOnFailureListener {
                    logError("UPDATE_TREE_DATA", "cannot update tree data at the specified path", it)
                    callback?.invoke(false)
                }
        }
    }

    override fun updateTreeDataChilds(path: String, field: String, value: Any, callback: ((Boolean) -> Unit)?) {
        readTreeDataChilds(path) { childs ->
            if (childs.isEmpty()) {
                logSuccess("UPDATE_TREE_DATA_CHILDS", "update tree data at the specified path success")
                callback?.invoke(true)
            }
            var completed = 0
            for (child in childs) {
                updateTreeData(child.completePath(), field, value) { updated ->
                    if (updated)
                        completed++
                    else {
                        logError("UPDATE_TREE_DATA_CHILDS", "cannot update tree data at the specified path")
                        callback?.invoke(false)
                    }

                    if (completed == childs.size) {
                        logSuccess("UPDATE_TREE_DATA_CHILDS", "update tree data at the specified path success")
                        callback?.invoke(true)
                    }
                }
            }
        }
    }

    override fun deleteTreeData(path: String, callback: ((Boolean) -> Unit)?) {
        deleteTreeDataChilds(path) { childsDeleted ->
            if (childsDeleted) {
                readTreeData(path) { treeData ->
                    getDocument(path)?.delete()
                        ?.addOnSuccessListener {
                            val tokens = path.split("/")
                            val metadataId = tokens[1]
                            if (tokens.size <= 2)
                                deleteMetadata(metadataId)
                            else
                                updateMapMetadata(metadataId, path, type = treeData?.type, delete = true)
                            logSuccess("DELETE_TREE_DATA", "delete tree data at the specified path success")
                            callback?.invoke(true)
                        }
                        ?.addOnFailureListener {
                            logError("DELETE_TREE_DATA", "cannot delete tree data at the specified path", it)
                            callback?.invoke(false)
                        }
                }
            }
            else {
                logError("DELETE_TREE_DATA", "cannot delete tree data at the specified path")
                callback?.invoke(false)
            }
        }
    }

    override fun deleteTreeDataChilds(path: String, callback: ((Boolean) -> Unit)?) {
        readTreeDataChilds(path) { childs ->
            if (childs.isEmpty()) {
                logSuccess("DELETE_TREE_DATA_CHILDS", "delete tree data at the specified path success")
                callback?.invoke(true)
            }

            var completed = 0
            for (child in childs) {
                deleteTreeData(child.completePath()) { deleted ->
                    if (deleted)
                        completed++
                    else {
                        logError("DELETE_TREE_DATA_CHILDS", "cannot delete tree data at the specified path")
                        callback?.invoke(false)
                    }

                    if (completed == childs.size) {
                        logSuccess("DELETE_TREE_DATA_CHILDS", "delete tree data at the specified path success")
                        callback?.invoke(true)
                    }
                }
            }
        }
    }

    override fun readAllType(rootId: String, value: Any, callback: (List<ITreeData>) -> Unit) {
        var source = Source.DEFAULT
        readMetadata(rootId) { metadata ->
            val allType = allType(value.toString())
            val lastRead = FirestorePreferences.getLastRead(allType, context)
            val lastUpdate = metadata?.updateMap?.get(allType)
            if (lastRead != null && lastUpdate?.isBeforeDateTime(lastRead) == true)
                source = Source.CACHE
            val rootDocumentPath = "${IFirestore.treedataCollection()}/$rootId"
            firestore.collectionGroup(IFirestore.treedataCollection())
                .whereGreaterThanOrEqualTo(ITreeData::path.name, rootDocumentPath)
                .whereLessThan(ITreeData::path.name, rootDocumentPath.nextAlphabeticString())
                .whereEqualTo(ITreeData::type.name, value).get(source)
                .addOnSuccessListener { documents ->
                    FirestorePreferences.setLastRead(allType, currentDate(), context)
                    val list = mutableListOf<ITreeData>()
                    for (document in documents) {
                        val classType = firestoreProperties?.dataTypeResolver?.invoke(document.get("type").toString())
                        if (classType != null)
                            list.add(document.toObject(classType))
                    }
                    logSuccess("READ_ALL_TYPE", "read data with the specified id success")
                    callback(list)
                }
                .addOnFailureListener {
                    logError("READ_ALL_TYPE", "cannot read data with the specified id", it)
                    callback(listOf())
                }
        }
    }

    private fun getMetadataCollection(): CollectionReference? {
        return firestore.collection(IFirestore.metadataCollection())
    }

    private fun getTreeDataCollection(): CollectionReference? {
        return firestore.collection(IFirestore.treedataCollection())
    }

    private fun getDocument(path: String): DocumentReference? {
        val tokens = path.split("/").drop(1)
        var collection = getTreeDataCollection()
        var document: DocumentReference? = null
        tokens.takeIf { it.size % 2 != 0 }?.forEachIndexed { index, token ->
            if (index % 2 == 0)
                document = collection?.document(token)
            else
                collection = document?.collection(token)
        }
        return document
    }

    private fun getCollection(path: String): CollectionReference? {
        val tokens = path.split("/").drop(1)
        var collection = getTreeDataCollection()
        var document: DocumentReference? = null
        tokens.takeIf { it.size % 2 == 0 }?.forEachIndexed { index, token ->
            if (index % 2 == 0)
                document = collection?.document(token)
            else
                collection = document?.collection(token)
        }
        return collection
    }

    private fun allType(type: String): String {
        return "all$type"
    }
}