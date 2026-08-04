package com.cars24.data.page

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cars24.sdui.schema.SduiJson
import kotlinx.coroutines.flow.first

class PageStateStore(private val dataStore: DataStore<Preferences>) {

    suspend fun read(pageId: String): Map<String, String> = decode(keyFor(pageId))

    suspend fun write(pageId: String, localState: Map<String, String>) {
        dataStore.edit { it[keyFor(pageId)] = SduiJson.format.encodeToString(localState) }
    }

    suspend fun clear(pageId: String) {
        dataStore.edit { it.remove(keyFor(pageId)) }
    }

    suspend fun readShared(): Map<String, String> = decode(SHARED_KEY)

    suspend fun writeShared(state: Map<String, String>) {
        dataStore.edit { it[SHARED_KEY] = SduiJson.format.encodeToString(state) }
    }

    private suspend fun decode(key: Preferences.Key<String>): Map<String, String> {
        val raw = dataStore.data.first()[key] ?: return emptyMap()
        return runCatching { SduiJson.format.decodeFromString<Map<String, String>>(raw) }
            .getOrDefault(emptyMap())
    }

    private fun keyFor(pageId: String) = stringPreferencesKey("ui_state_$pageId")

    private companion object {
        val SHARED_KEY = stringPreferencesKey("ui_state_shared")
    }
}
