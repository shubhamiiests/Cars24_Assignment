package com.cars24.data.page

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cars24.sdui.schema.SduiJson
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable

@Serializable
data class PersistedPageState(
    val localState: Map<String, String> = emptyMap(),
    val scrollIndex: Int = 0,
    val scrollOffset: Int = 0,
    val openSheetId: String? = null,
)

class PageStateStore(private val dataStore: DataStore<Preferences>) {

    suspend fun read(pageId: String): PersistedPageState {
        val raw = dataStore.data.first()[keyFor(pageId)] ?: return PersistedPageState()
        return runCatching { SduiJson.format.decodeFromString<PersistedPageState>(raw) }
            .getOrDefault(PersistedPageState())
    }

    suspend fun write(pageId: String, state: PersistedPageState) {
        val encoded = SduiJson.format.encodeToString(state)
        dataStore.edit { it[keyFor(pageId)] = encoded }
    }

    suspend fun clear(pageId: String) {
        dataStore.edit { it.remove(keyFor(pageId)) }
    }

    suspend fun readShared(): Map<String, String> {
        val raw = dataStore.data.first()[SHARED_KEY] ?: return emptyMap()
        return runCatching { SduiJson.format.decodeFromString<Map<String, String>>(raw) }
            .getOrDefault(emptyMap())
    }

    suspend fun writeShared(state: Map<String, String>) {
        val encoded = SduiJson.format.encodeToString(state)
        dataStore.edit { it[SHARED_KEY] = encoded }
    }

    private fun keyFor(pageId: String) = stringPreferencesKey("ui_state_$pageId")

    private companion object {
        val SHARED_KEY = stringPreferencesKey("ui_state_shared")
    }
}
