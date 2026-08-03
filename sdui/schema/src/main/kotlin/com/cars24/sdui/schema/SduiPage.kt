package com.cars24.sdui.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class SduiPage(
    val pageId: String,
    val schemaVersion: Int = 1,
    val title: String? = null,
    val analyticsName: String? = null,
    val background: String? = null,
    val initialState: Map<String, String> = emptyMap(),
    val sharedStateKeys: List<String> = emptyList(),
    val sections: List<SduiNode> = emptyList(),
)

@Serializable
data class SduiNode(
    val id: String,
    val type: String,
    val props: JsonObject = EmptyProps,
    val style: SduiStyle? = null,
    val children: List<SduiNode> = emptyList(),
    val actions: Map<String, SduiAction> = emptyMap(),
    val visibleWhen: SduiCondition? = null,
    val minSchemaVersion: Int = 1,
    val fallback: SduiNode? = null,
)

@Serializable
data class SduiAction(
    val type: String,
    val params: Map<String, String> = emptyMap(),
    val content: List<SduiNode> = emptyList(),
    val then: List<SduiAction> = emptyList(),
)

internal val EmptyProps = JsonObject(emptyMap())
