package com.cars24.feature.sduipage

import androidx.lifecycle.viewModelScope
import com.cars24.core.analytics.AnalyticsEvents
import com.cars24.core.analytics.AnalyticsLogger
import com.cars24.core.analytics.AnalyticsParams
import com.cars24.core.common.mvi.MviViewModel
import com.cars24.data.page.PageEnvelope
import com.cars24.data.page.PageLoadResult
import com.cars24.data.page.PageStateStore
import com.cars24.data.page.PersistedPageState
import com.cars24.data.page.SduiPageRepository
import com.cars24.sdui.runtime.action.SduiActionParser
import com.cars24.sdui.runtime.action.SduiCommand
import com.cars24.sdui.schema.SduiAction
import com.cars24.sdui.schema.SduiNode
import com.cars24.sdui.schema.SduiPage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SduiPageViewModel(
    private val pageId: String,
    private val routeParams: Map<String, String> = emptyMap(),
    private val repository: SduiPageRepository,
    private val pageStateStore: PageStateStore,
    private val analytics: AnalyticsLogger,
) : MviViewModel<PageIntent, PageUiState, PageEffect>(PageUiState()) {

    private var persistJob: Job? = null
    private var loaded = false
    private var sharedKeys: Set<String> = emptySet()

    init {
        dispatch(PageIntent.Load)
    }

    override suspend fun handleIntent(intent: PageIntent) {
        when (intent) {
            PageIntent.Load -> if (!loaded) {
                loaded = true
                load()
            }

            PageIntent.Retry -> {
                setState { copy(phase = PagePhase.Loading, failureMessage = null) }
                load()
            }

            is PageIntent.Command -> execute(intent.command)

            is PageIntent.UnsupportedComponent -> onUnsupportedComponent(intent.type)

            is PageIntent.ScrollChanged -> {
                setState { copy(scrollIndex = intent.index, scrollOffset = intent.offset) }
                schedulePersist()
            }

            PageIntent.DismissSheet -> {
                setState { copy(openSheet = null) }
                schedulePersist()
            }
        }
    }

    private suspend fun load() {
        val restored = pageStateStore.read(pageId)
        val shared = pageStateStore.readShared()

        when (val result = repository.loadPage(pageId)) {
            is PageLoadResult.Loaded -> applyPage(result.envelope, restored, shared, staleMessage = null)

            is PageLoadResult.Stale ->
                applyPage(result.envelope, restored, shared, staleMessage = result.reason)

            PageLoadResult.Offline -> {
                setState { copy(phase = PagePhase.Offline, page = null) }
                analytics.logEvent(
                    AnalyticsEvents.SDUI_PAGE_OFFLINE,
                    mapOf(AnalyticsParams.PAGE_ID to pageId),
                )
            }

            is PageLoadResult.Failed ->
                setState { copy(phase = PagePhase.Failed, failureMessage = result.message) }
        }
    }

    private fun applyPage(
        envelope: PageEnvelope,
        restored: PersistedPageState,
        shared: Map<String, String>,
        staleMessage: String?,
    ) {
        val page = envelope.page
        sharedKeys = page.sharedStateKeys.toSet()

        val mergedState = page.initialState +
            restored.localState +
            routeParams +
            shared.filterKeys { it in sharedKeys }

        setState {
            copy(
                phase = PagePhase.Content,
                page = page,
                pageState = mergedState,
                openSheet = restored.openSheetId?.let { findSheet(page, it, mergedState) },
                scrollIndex = restored.scrollIndex,
                scrollOffset = restored.scrollOffset,
                staleMessage = staleMessage,
                failureMessage = null,
                origin = envelope.origin,
                fetchMillis = envelope.fetchMillis,
                parseMillis = envelope.parseMillis,
                payloadBytes = envelope.payloadBytes,
            )
        }

        analytics.logScreenView(page.analyticsName ?: pageId)
        analytics.logEvent(
            AnalyticsEvents.SDUI_PAGE_RENDERED,
            mapOf(
                AnalyticsParams.PAGE_ID to page.pageId,
                AnalyticsParams.ORIGIN to envelope.origin.name,
                AnalyticsParams.FETCH_MS to envelope.fetchMillis.toString(),
                AnalyticsParams.PARSE_MS to envelope.parseMillis.toString(),
                AnalyticsParams.PAYLOAD_BYTES to envelope.payloadBytes.toString(),
                AnalyticsParams.SECTION_COUNT to page.sections.size.toString(),
            ),
        )
    }

    private suspend fun execute(command: SduiCommand) {
        when (command) {
            is SduiCommand.Batch -> command.commands.forEach { execute(it) }

            is SduiCommand.SetState -> putState(command.key, command.value)

            is SduiCommand.ToggleState -> putState(
                key = command.key,
                value = if (currentState.pageState[command.key] == command.onValue) {
                    command.offValue
                } else {
                    command.onValue
                },
            )

            is SduiCommand.OpenSheet -> {
                setState {
                    copy(openSheet = OpenSheet(command.sheetId, command.title, command.content))
                }
                schedulePersist()
            }

            SduiCommand.DismissSheet -> {
                setState { copy(openSheet = null) }
                schedulePersist()
            }

            is SduiCommand.Navigate ->
                emitEffect(PageEffect.Navigate(command.route, command.params))

            is SduiCommand.OpenUrl -> emitEffect(PageEffect.OpenUrl(command.url))

            is SduiCommand.Track -> analytics.logEvent(command.event, command.params)

            SduiCommand.Refresh -> {
                setState { copy(phase = PagePhase.Loading) }
                load()
            }

            is SduiCommand.Unsupported -> {
                analytics.logEvent(
                    AnalyticsEvents.SDUI_ACTION_UNSUPPORTED,
                    mapOf(AnalyticsParams.ACTION_TYPE to command.type),
                )
                emitEffect(PageEffect.ShowMessage("Update the app to use this"))
            }
        }
    }

    private fun onUnsupportedComponent(type: String) {
        if (type in currentState.unsupportedTypes) return
        setState { copy(unsupportedTypes = unsupportedTypes + type) }
        analytics.logEvent(
            AnalyticsEvents.SDUI_UNSUPPORTED_COMPONENT,
            mapOf(
                AnalyticsParams.COMPONENT_TYPE to type,
                AnalyticsParams.PAGE_ID to pageId,
            ),
        )
    }

    private fun putState(key: String, value: String) {
        setState { copy(pageState = pageState + (key to value)) }
        schedulePersist()
    }

    private fun schedulePersist() {
        persistJob?.cancel()
        persistJob = viewModelScope.launch {
            delay(PERSIST_DEBOUNCE_MS)
            val snapshot = currentState
            pageStateStore.write(
                pageId,
                PersistedPageState(
                    localState = snapshot.pageState.filterKeys { it !in sharedKeys },
                    scrollIndex = snapshot.scrollIndex,
                    scrollOffset = snapshot.scrollOffset,
                    openSheetId = snapshot.openSheet?.sheetId,
                ),
            )
            if (sharedKeys.isNotEmpty()) {
                pageStateStore.writeShared(
                    pageStateStore.readShared() +
                        snapshot.pageState.filterKeys { it in sharedKeys },
                )
            }
        }
    }

    private companion object {
        const val PERSIST_DEBOUNCE_MS = 250L
    }
}

private fun findSheet(page: SduiPage, sheetId: String, state: Map<String, String>): OpenSheet? {
    fun search(nodes: List<SduiNode>): OpenSheet? {
        for (node in nodes) {
            node.actions.values.forEach { action ->
                findInAction(action, sheetId, state)?.let { return it }
            }
            search(node.children)?.let { return it }
            node.fallback?.let { search(listOf(it)) }?.let { return it }
        }
        return null
    }
    return search(page.sections)
}

private fun findInAction(
    action: SduiAction,
    sheetId: String,
    state: Map<String, String>,
): OpenSheet? {
    val command = SduiActionParser.parse(action, state)
    flatten(command).forEach { candidate ->
        if (candidate is SduiCommand.OpenSheet && candidate.sheetId == sheetId) {
            return OpenSheet(candidate.sheetId, candidate.title, candidate.content)
        }
    }
    return null
}

private fun flatten(command: SduiCommand): List<SduiCommand> = when (command) {
    is SduiCommand.Batch -> command.commands.flatMap(::flatten)
    else -> listOf(command)
}
