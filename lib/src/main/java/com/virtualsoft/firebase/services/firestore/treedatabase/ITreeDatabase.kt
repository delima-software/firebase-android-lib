package com.virtualsoft.firebase.services.firestore.treedatabase

interface ITreeDatabase {

    fun readAllType(rootId: String, value: Any, callback: (List<ITreeData>) -> Unit)

    fun readTreeData(path: String, callback: (ITreeData?) -> Unit)

    fun readTreeDataChilds(path: String, callback: (List<ITreeData>) -> Unit)

    fun writeTreeData(path: String, data: ITreeData, callback: ((Boolean) -> Unit)? = null)

    fun writeTreeDataChilds(path: String, childs: List<ITreeData>, callback: ((Boolean) -> Unit)? = null)

    fun updateTreeData(path: String, field: String, value: Any, callback: ((Boolean) -> Unit)? = null)

    fun updateTreeDataChilds(path: String, field: String, value: Any, callback: ((Boolean) -> Unit)? = null)

    fun deleteTreeData(path: String, callback: ((Boolean) -> Unit)? = null)

    fun deleteTreeDataChilds(path: String, callback: ((Boolean) -> Unit)? = null)
}