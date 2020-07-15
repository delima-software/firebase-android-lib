package com.virtualsoft.firebase.data.database

import android.content.Context
import com.virtualsoft.core.designpatterns.builder.IBuilder
import com.virtualsoft.core.utils.DateUtils
import com.virtualsoft.core.utils.GeneratorUtils
import com.virtualsoft.firebase.R
import java.util.*

class Metadata(override var id: String? = null,
               override var name: String? = null,
               override var type: String? = null,
               override var creationDate: Date? = null,
               override var lastUpdate: Date? = null) : IMetadata {

    companion object {

        fun buildMetadata(metadataId: String, context: Context?): Metadata {
            return Builder(context)
                .setId(metadataId)
                .build()
        }
    }

    class Builder(context: Context? = null) : IBuilder<Metadata> {

        override val building =
            Metadata(
                id = GeneratorUtils.generateUUID(),
                name = context?.resources?.getString(R.string.default_metadata_name),
                type = Metadata::class.java.simpleName,
                creationDate = DateUtils.currentDate(),
                lastUpdate = DateUtils.currentDate()
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
    }
}