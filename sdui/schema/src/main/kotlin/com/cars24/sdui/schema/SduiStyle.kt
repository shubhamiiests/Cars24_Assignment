package com.cars24.sdui.schema

import kotlinx.serialization.Serializable


@Serializable
data class SduiStyle(
    val padding: SduiEdges? = null,
    val margin: SduiEdges? = null,
    val background: String? = null,
    val gradient: List<String>? = null,
    val cornerRadius: Int? = null,
    val borderWidth: Int? = null,
    val borderColor: String? = null,
    val elevation: Int? = null,
    val width: String? = null,
    val height: String? = null,
    val aspectRatio: Float? = null,
    val alpha: Float? = null,
)

@Serializable
data class SduiEdges(
    val all: Int? = null,
    val horizontal: Int? = null,
    val vertical: Int? = null,
    val start: Int? = null,
    val top: Int? = null,
    val end: Int? = null,
    val bottom: Int? = null,
) {
    val resolvedStart: Int get() = start ?: horizontal ?: all ?: 0
    val resolvedTop: Int get() = top ?: vertical ?: all ?: 0
    val resolvedEnd: Int get() = end ?: horizontal ?: all ?: 0
    val resolvedBottom: Int get() = bottom ?: vertical ?: all ?: 0
}
