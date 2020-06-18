package com.virtualsoft.firebase.data

import android.content.Context
import com.virtualsoft.core.service.database.data.ITreeData
import java.util.*
import kotlin.collections.HashMap

interface IMetadata : ITreeData {
    var updateMap: HashMap<String, Date>?

    companion object {

        fun buildMetadata(metadataId: String, context: Context?): IMetadata {
            return Metadata.Builder(context)
                .setId(metadataId)
                .build()
        }
    }
}