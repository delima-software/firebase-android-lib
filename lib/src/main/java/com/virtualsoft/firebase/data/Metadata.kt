package com.virtualsoft.firebase.data

import android.content.Context
import com.virtualsoft.core.designpatterns.builder.IBuilder
import com.virtualsoft.core.utils.DateUtils.currentDate
import com.virtualsoft.core.utils.GeneratorUtils.generateUUID
import com.virtualsoft.firebase.R
import com.virtualsoft.firebase.services.firestore.IFirestore
import java.util.*
import kotlin.collections.HashMap

class Metadata(override var id: String? = null,
               override var name: String? = null,
               override var type: String? = null,
               override var creationDate: Date? = null,
               override var lastUpdate: Date? = null,
               override var path: String? = null,
               override var updateMap: HashMap<String, Date>? = null) : IMetadata {

    class Builder(context: Context? = null) : IBuilder<IMetadata> {

        override val building = Metadata(
            id = generateUUID(),
            name = context?.resources?.getString(R.string.default_metadata_name),
            type = Metadata::class.java.simpleName,
            creationDate = currentDate(),
            lastUpdate = currentDate(),
            path = IFirestore.metadataCollection(context),
            updateMap = hashMapOf()
        )

        fun setId(id: String?): Builder {
            building.id = id
            return this
        }

        fun setName(name: String?): Builder {
            building.name = name
            return this
        }

        fun setType(type: String?): Builder {
            building.type = type
            return this
        }

        fun setCreationDate(creationDate: Date?): Builder {
            building.creationDate = creationDate
            return this
        }

        fun setLastUpdate(lastUpdate: Date?): Builder {
            building.lastUpdate = lastUpdate
            return this
        }

        fun setPath(path: String?): Builder {
            building.path = path
            return this
        }

        fun setUpdateMap(updateMap: HashMap<String, Date>?): Builder {
            building.updateMap = updateMap
            return this
        }
    }
}