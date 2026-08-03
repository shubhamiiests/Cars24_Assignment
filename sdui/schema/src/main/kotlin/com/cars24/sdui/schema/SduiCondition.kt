package com.cars24.sdui.schema

import kotlinx.serialization.Serializable

@Serializable
data class SduiCondition(
    val key: String,
    val equals: String? = null,
    val notEquals: String? = null,
    val oneOf: List<String>? = null,
    val exists: Boolean? = null,
) {
    fun evaluate(state: Map<String, String>): Boolean {
        val value = state[key]

        exists?.let { mustExist ->
            if ((value != null) != mustExist) return false
        }
        equals?.let { if (value != it) return false }
        notEquals?.let { if (value == it) return false }
        oneOf?.let { if (value == null || value !in it) return false }

        return true
    }
}
