package com.cars24.core.analytics

interface AnalyticsLogger {

    fun logEvent(name: String, params: Map<String, String> = emptyMap())

    fun logScreenView(screenName: String)

    val isActive: Boolean
}

object AnalyticsEvents {
    const val SCREEN_VIEW = "screen_view"
    const val SDUI_UNSUPPORTED_COMPONENT = "sdui_unsupported_component"

    const val SDUI_PAGE_RENDERED = "sdui_page_rendered"
    const val SDUI_ACTION_UNSUPPORTED = "sdui_action_unsupported"
    const val SDUI_PAGE_OFFLINE = "sdui_page_offline"
}

object AnalyticsParams {
    const val COMPONENT_TYPE = "component_type"
    const val ACTION_TYPE = "action_type"
    const val PAGE_ID = "page_id"
    const val ORIGIN = "origin"
    const val FETCH_MS = "fetch_ms"
    const val PARSE_MS = "parse_ms"
    const val PAYLOAD_BYTES = "payload_bytes"
    const val SECTION_COUNT = "section_count"
}

class NoOpAnalyticsLogger : AnalyticsLogger {
    override fun logEvent(name: String, params: Map<String, String>) = Unit
    override fun logScreenView(screenName: String) = Unit
    override val isActive: Boolean = false
}
