package com.virtualsoft.firebase.services.firestore.treedatabase

import com.virtualsoft.core.service.database.data.IData

interface ITreeData : IData {

    var path: String?

    fun completePath(): String {
        return "$path/$id"
    }

}