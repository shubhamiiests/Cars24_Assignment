package com.cars24.data.page

import com.cars24.sdui.schema.SduiPage

enum class PageOrigin {
    Network,
    Cache,
    Override,
}

data class PageEnvelope(
    val page: SduiPage,
    val origin: PageOrigin,
    val fetchMillis: Long,
    val parseMillis: Long,
    val payloadBytes: Int,
)

sealed interface PageLoadResult {

    data class Loaded(val envelope: PageEnvelope) : PageLoadResult
    data class Stale(val envelope: PageEnvelope, val reason: String) : PageLoadResult
    data object Offline : PageLoadResult
    data class Failed(val message: String) : PageLoadResult
}
