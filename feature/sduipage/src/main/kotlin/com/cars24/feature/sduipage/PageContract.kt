package com.cars24.feature.sduipage

import com.cars24.core.common.mvi.MviEffect
import com.cars24.core.common.mvi.MviIntent
import com.cars24.core.common.mvi.MviState
import com.cars24.data.page.PageOrigin
import com.cars24.sdui.runtime.action.SduiCommand
import com.cars24.sdui.schema.SduiNode
import com.cars24.sdui.schema.SduiPage

sealed interface PageIntent : MviIntent {

    data object Load : PageIntent
    data object Retry : PageIntent
    data class Command(val command: SduiCommand) : PageIntent
    data class UnsupportedComponent(val type: String) : PageIntent
    data class ScrollChanged(val index: Int, val offset: Int) : PageIntent
    data object DismissSheet : PageIntent
}

enum class PagePhase { Loading, Content, Offline, Failed }

data class PageUiState(
    val phase: PagePhase = PagePhase.Loading,
    val page: SduiPage? = null,
    val pageState: Map<String, String> = emptyMap(),
    val openSheet: OpenSheet? = null,
    val scrollIndex: Int = 0,
    val scrollOffset: Int = 0,
    val staleMessage: String? = null,
    val failureMessage: String? = null,
    val origin: PageOrigin? = null,
    val fetchMillis: Long = 0,
    val parseMillis: Long = 0,
    val payloadBytes: Int = 0,
    val unsupportedTypes: Set<String> = emptySet(),
) : MviState

data class OpenSheet(
    val sheetId: String,
    val title: String?,
    val content: List<SduiNode>,
)

sealed interface PageEffect : MviEffect {
    data class Navigate(val route: String, val params: Map<String, String>) : PageEffect
    data class OpenUrl(val url: String) : PageEffect
    data class ShowMessage(val message: String) : PageEffect
}
