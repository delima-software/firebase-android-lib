package com.virtualsoft.firebase.services.firestore.database

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query

interface IFirestoreDatabase {

    suspend fun readDocument(documentReference: DocumentReference): IDocument?

    suspend fun readCollection(collectionReference: CollectionReference): List<IDocument>

    suspend fun readCollection(collectionId: String, query: Query): List<IDocument>

    suspend fun writeDocument(documentReference: DocumentReference, data: IDocument): Boolean

    suspend fun updateDocument(documentReference: DocumentReference, field: String, value: Any): Boolean

    suspend fun deleteDocument(documentReference: DocumentReference): Boolean
}