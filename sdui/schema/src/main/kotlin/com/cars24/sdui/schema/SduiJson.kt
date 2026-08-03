package com.cars24.sdui.schema

import kotlinx.serialization.json.Json

object SduiJson {

    const val SUPPORTED_SCHEMA_VERSION = 2

    val format: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
        coerceInputValues = true
    }
}
