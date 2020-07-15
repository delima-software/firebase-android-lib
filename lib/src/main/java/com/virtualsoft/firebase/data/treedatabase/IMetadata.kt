package com.virtualsoft.firebase.data.treedatabase

import com.virtualsoft.core.service.database.data.ITreeData
import java.util.*
import kotlin.collections.HashMap

interface IMetadata : ITreeData {
    var updateMap: HashMap<String, Date>?
}