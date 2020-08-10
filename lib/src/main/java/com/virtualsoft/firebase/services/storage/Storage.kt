package com.virtualsoft.firebase.services.storage

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.virtualsoft.core.designpatterns.builder.IBuilder
import com.virtualsoft.firebase.utils.LogUtils
import com.virtualsoft.firebase.services.storage.IStorage.DownloadProperties
import com.virtualsoft.firebase.services.storage.IStorage.UploadProperties
import java.io.File
import java.io.InputStream

class Storage : IStorage {

    override val id = Storage::class.java.name

    private val storage by lazy {
        Firebase.storage
    }

    class Builder : IBuilder<Storage> {

        override val building = Storage()
    }

    private fun addUploadProperties(uploadTask: UploadTask, properties: UploadProperties) {
        uploadTask
            .addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                properties.progressListener?.invoke(progress)
            }
            .addOnPausedListener {
                properties.pausedListener?.invoke()
            }
            .addOnCompleteListener {
                properties.completedListener?.invoke()
            }
            .addOnSuccessListener { taskSnapshot ->
                val downloadUrl = taskSnapshot.storage.downloadUrl
                properties.downloadUrlListener?.invoke(downloadUrl.result)
                properties.successListener?.invoke()
            }
            .addOnCanceledListener {
                properties.canceledListener?.invoke()
            }
            .addOnFailureListener { exception ->
                LogUtils.logError(
                    "UPLOAD_STORAGE",
                    "failed to upload data",
                    exception)
                properties.failureListener?.invoke()
            }
    }

    private fun addByteArrayDownloadProperties(downloadTask: Task<ByteArray>, properties: DownloadProperties) {
        downloadTask
            .addOnCompleteListener {
                properties.completedListener?.invoke()
            }
            .addOnSuccessListener { byteArray ->
                properties.byteArrayListener?.invoke(byteArray)
                properties.successListener?.invoke()
            }
            .addOnCanceledListener {
                properties.canceledListener?.invoke()
            }
            .addOnFailureListener { exception ->
                LogUtils.logError(
                    "UPLOAD_STORAGE",
                    "failed to download data",
                    exception)
                properties.failureListener?.invoke()
            }
    }

    private fun addFileDownloadProperties(downloadTask: FileDownloadTask, properties: DownloadProperties) {
        downloadTask
            .addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                properties.progressListener?.invoke(progress)
            }
            .addOnPausedListener {
                properties.pausedListener?.invoke()
            }
            .addOnCompleteListener {
                properties.completedListener?.invoke()
            }
            .addOnSuccessListener { taskSnapshot ->
                val downloadUrl = taskSnapshot.storage.downloadUrl
                properties.downloadUrlListener?.invoke(downloadUrl.result)
                properties.successListener?.invoke()
            }
            .addOnCanceledListener {
                properties.canceledListener?.invoke()
            }
            .addOnFailureListener { exception ->
                LogUtils.logError(
                    "UPLOAD_STORAGE",
                    "failed to download data",
                    exception)
                properties.failureListener?.invoke()
            }
    }

    override fun upload(bytes: ByteArray, path: String, properties: UploadProperties?): UploadTask {
        val reference = storage.reference.child(path)
        val task = if (properties?.metadata != null )
            reference.putBytes(bytes, properties.metadata!!)
        else
            reference.putBytes(bytes)
        properties?.let {
            addUploadProperties(task, properties)
        }
        return task
    }

    override fun upload(stream: InputStream, path: String, properties: UploadProperties?): UploadTask {
        val reference = storage.reference.child(path)
        val task = if (properties?.metadata != null )
            reference.putStream(stream, properties.metadata!!)
        else
            reference.putStream(stream)
        properties?.let {
            addUploadProperties(task, properties)
        }
        return task
    }

    override fun upload(fileUri: Uri, path: String, properties: UploadProperties?): UploadTask {
        val reference = storage.reference.child(path)
        val task = if (properties?.metadata != null )
            reference.putFile(fileUri, properties.metadata!!)
        else
            reference.putFile(fileUri)
        properties?.let {
            addUploadProperties(task, properties)
        }
        return task
    }

    override fun download(path: String, properties: DownloadProperties): Task<ByteArray>? {
        properties.maxBytes?.let { maxBytes ->
            val reference = storage.reference.child(path)
            val task = reference.getBytes(maxBytes)
            addByteArrayDownloadProperties(task, properties)
            return task
        }
        return null
    }

    override fun download(file: File, path: String, properties: DownloadProperties): FileDownloadTask {
        val reference = storage.reference.child(path)
        val task = reference.getFile(file)
        addFileDownloadProperties(task, properties)
        return task
    }
}