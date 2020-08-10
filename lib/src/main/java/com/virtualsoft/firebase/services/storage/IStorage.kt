package com.virtualsoft.firebase.services.storage

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import com.virtualsoft.core.designpatterns.builder.IBuild
import com.virtualsoft.firebase.IFirebase
import java.io.File
import java.io.InputStream

interface IStorage : IFirebase {

    data class UploadProperties(
        var metadata: StorageMetadata? = null,
        var progressListener: ((Double) -> Unit)? = null,
        var pausedListener: (() -> Unit)? = null,
        var completedListener: (() -> Unit)? = null,
        var downloadUrlListener: ((Uri?) -> Unit)? = null,
        var successListener: (() -> Unit)? = null,
        var canceledListener: (() -> Unit)? = null,
        var failureListener: (() -> Unit)? = null
    ) : IBuild

    data class DownloadProperties(
        var maxBytes: Long? = null,
        var progressListener: ((Double) -> Unit)? = null,
        var pausedListener: (() -> Unit)? = null,
        var completedListener: (() -> Unit)? = null,
        var downloadUrlListener: ((Uri?) -> Unit)? = null,
        var byteArrayListener: ((ByteArray) -> Unit)? = null,
        var successListener: (() -> Unit)? = null,
        var canceledListener: (() -> Unit)? = null,
        var failureListener: (() -> Unit)? = null
    ) : IBuild

    fun upload(bytes: ByteArray, path: String, properties: UploadProperties? = null): UploadTask

    fun upload(stream: InputStream, path: String, properties: UploadProperties? = null): UploadTask

    fun upload(fileUri: Uri, path: String, properties: UploadProperties? = null): UploadTask

    fun download(path: String, properties: DownloadProperties): Task<ByteArray>?

    fun download(file: File, path: String, properties: DownloadProperties): FileDownloadTask
}