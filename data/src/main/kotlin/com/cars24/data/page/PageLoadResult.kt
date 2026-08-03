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

enum class PageFailure {
    ServerPayloadUnparseable,
    PushedPayloadUnparseable,
}

enum class StaleReason {
    NoConnection,
    ServerPayloadUnusable,
}

sealed interface PageLoadResult {

    data class Loaded(val envelope: PageEnvelope) : PageLoadResult
    data class Stale(val envelope: PageEnvelope, val reason: StaleReason) : PageLoadResult
    data object Offline : PageLoadResult
    data class Failed(val failure: PageFailure) : PageLoadResult
}
