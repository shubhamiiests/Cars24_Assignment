package com.cars24.feature.sduipage

import androidx.lifecycle.viewModelScope
import com.cars24.core.analytics.AnalyticsEvents
import com.cars24.core.analytics.AnalyticsLogger
import com.cars24.core.analytics.AnalyticsParams
import com.cars24.core.common.mvi.MviViewModel
import com.cars24.core.common.perf.StartupTrace
import com.cars24.data.page.PageEnvelope
import com.cars24.data.page.PageLoadResult
import com.cars24.data.page.StaleReason
import com.cars24.data.page.PageStateStore
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
                setState { copy(phase = PagePhase.Loading, failure = null) }
                load()
            }

            PageIntent.Refresh -> {
                setState { copy(isRefreshing = true) }
                load()
                setState { copy(isRefreshing = false) }
            }

            is PageIntent.Command -> execute(intent.command)

            is PageIntent.UnsupportedComponent -> onUnsupportedComponent(intent.type)

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
            is PageLoadResult.Loaded -> applyPage(result.envelope, restored, shared, staleReason = null)

            is PageLoadResult.Stale ->
                applyPage(result.envelope, restored, shared, staleReason = result.reason)

            PageLoadResult.Offline -> {
                setState { copy(phase = PagePhase.Offline, page = null) }
                analytics.logEvent(
                    AnalyticsEvents.SDUI_PAGE_OFFLINE,
                    mapOf(AnalyticsParams.PAGE_ID to pageId),
                )
            }

            is PageLoadResult.Failed ->
                setState { copy(phase = PagePhase.Failed, failure = result.failure) }
        }
    }

    private fun applyPage(
        envelope: PageEnvelope,
        restored: Map<String, String>,
        shared: Map<String, String>,
        staleReason: StaleReason?,
    ) {
        val page = envelope.page
        sharedKeys = page.sharedStateKeys.toSet()

        val mergedState = page.initialState +
            restored +
            routeParams +
            shared.filterKeys { it in sharedKeys }

        setState {
            copy(
                phase = PagePhase.Content,
                page = page,
                pageState = mergedState,
                staleReason = staleReason,
                failure = null,
                origin = envelope.origin,
                fetchMillis = envelope.fetchMillis,
                parseMillis = envelope.parseMillis,
                payloadBytes = envelope.payloadBytes,
            )
        }

        android.util.Log.i(
            StartupTrace.LOG_TAG,
            "payload origin=${envelope.origin} bytes=${envelope.payloadBytes} " +
                "fetch=${envelope.fetchMillis}ms parse=${envelope.parseMillis}ms " +
                "sections=${page.sections.size}",
        )

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

            SduiCommand.Refresh -> handleIntent(PageIntent.Refresh)

            is SduiCommand.Unsupported -> {
                analytics.logEvent(
                    AnalyticsEvents.SDUI_ACTION_UNSUPPORTED,
                    mapOf(AnalyticsParams.ACTION_TYPE to command.type),
                )
                emitEffect(PageEffect.UnsupportedAction)
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
                snapshot.pageState.filterKeys { it !in sharedKeys },
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

