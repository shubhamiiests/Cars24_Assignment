package com.cars24.data.page

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

class PagePayloadCache(private val dataStore: DataStore<Preferences>) {

    suspend fun read(pageId: String): String? =
        dataStore.data.first()[keyFor(pageId)]

    suspend fun write(pageId: String, payload: String) {
        dataStore.edit { it[keyFor(pageId)] = payload }
    }

    suspend fun clear(pageId: String) {
        dataStore.edit { it.remove(keyFor(pageId)) }
    }

    private fun keyFor(pageId: String) = stringPreferencesKey("payload_$pageId")
}
