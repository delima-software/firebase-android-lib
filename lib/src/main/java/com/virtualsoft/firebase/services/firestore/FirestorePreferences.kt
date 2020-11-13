package com.virtualsoft.firebase.services.firestore

import android.content.Context
import com.virtualsoft.core.utils.DateUtils.dateInstance
import com.virtualsoft.core.utils.DateUtils.timeInMillis
import java.util.*

internal object FirestorePreferences {

    private const val preferenceFileKey = "firestore_preferences"

    fun getLastRead(path: String, context: Context?): Date? {
        var date: Date? = null
        context?.let {
            val preferences = it.getSharedPreferences(preferenceFileKey, Context.MODE_PRIVATE)
            preferences.getLong(path, Long.MIN_VALUE).let { dateStored ->
                if (dateStored != Long.MIN_VALUE)
                    date = dateInstance(dateStored)
            }
        }
        return date
    }

    fun setLastRead(path: String, date: Date, context: Context?) {
        context?.let {
            val preferences = it.getSharedPreferences(preferenceFileKey, Context.MODE_PRIVATE)
            val timeInMillis = timeInMillis(date)
            preferences.edit().putLong(path, timeInMillis).apply()
        }
    }

    fun resetLastRead(path: String, context: Context?) {
        context?.let {
            val preferences = it.getSharedPreferences(preferenceFileKey, Context.MODE_PRIVATE)
            preferences.edit().putLong(path, Long.MIN_VALUE).apply()
        }
    }
}