package com.cars24.sdui.schema

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

object SduiTemplate {

    private val PLACEHOLDER = Regex("""\{\{\s*state\.([A-Za-z0-9_.-]+)\s*(?:\|([^}]*))?\}\}""")

    fun containsPlaceholder(text: String): Boolean = text.contains("{{")

    fun resolve(text: String, state: Map<String, String>): String {
        if (!containsPlaceholder(text)) return text
        return PLACEHOLDER.replace(text) { match ->
            val key = match.groupValues[1]
            val fallback = match.groupValues.getOrNull(2).orEmpty()
            state[key] ?: fallback.ifEmpty { match.value }
        }
    }

    fun resolve(element: JsonElement, state: Map<String, String>): JsonElement = when (element) {
        is JsonPrimitive -> resolvePrimitive(element, state)
        is JsonObject -> {
            var changed = false
            val mapped = LinkedHashMap<String, JsonElement>(element.size)
            for ((key, value) in element) {
                val resolved = resolve(value, state)
                if (resolved !== value) changed = true
                mapped[key] = resolved
            }
            if (changed) JsonObject(mapped) else element
        }
        is JsonArray -> {
            var changed = false
            val mapped = ArrayList<JsonElement>(element.size)
            for (value in element) {
                val resolved = resolve(value, state)
                if (resolved !== value) changed = true
                mapped.add(resolved)
            }
            if (changed) JsonArray(mapped) else element
        }
    }

    private fun resolvePrimitive(primitive: JsonPrimitive, state: Map<String, String>): JsonElement {
        if (!primitive.isString) return primitive
        val resolved = resolve(primitive.content, state)
        return if (resolved == primitive.content) primitive else JsonPrimitive(resolved)
    }

    fun resolve(params: Map<String, String>, state: Map<String, String>): Map<String, String> {
        if (params.isEmpty()) return params
        return params.mapValues { (_, value) -> resolve(value, state) }
    }
}
