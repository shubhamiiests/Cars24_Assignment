package com.cars24.data.page

import com.cars24.core.common.coroutines.DispatcherProvider
import com.cars24.core.common.network.ConnectivityMonitor
import com.cars24.core.common.perf.StartupTrace
import com.cars24.sdui.schema.SduiJson
import com.cars24.sdui.schema.SduiPage
import kotlinx.coroutines.withContext

interface SduiPageRepository {
    suspend fun loadPage(pageId: String): PageLoadResult

    suspend fun availablePages(): Set<String>
}

class SduiPageRepositoryImpl(
    private val remote: PageDataSource,
    private val override: FileOverridePageDataSource,
    private val payloadCache: PagePayloadCache,
    private val connectivity: ConnectivityMonitor,
    private val dispatchers: DispatcherProvider,
) : SduiPageRepository {

    override suspend fun loadPage(pageId: String): PageLoadResult = withContext(dispatchers.io) {
        if (override.hasOverride(pageId)) {
            return@withContext load(pageId, override, PageOrigin.Override, cache = false)
                ?: PageLoadResult.Failed(PageFailure.PushedPayloadUnparseable)
        }

        if (connectivity.isOnline) {
            load(pageId, remote, PageOrigin.Network, cache = true)?.let { return@withContext it }
            return@withContext fromCache(pageId, StaleReason.ServerPayloadUnusable)
                ?: PageLoadResult.Failed(PageFailure.ServerPayloadUnparseable)
        }

        fromCache(pageId, StaleReason.NoConnection)
            ?: PageLoadResult.Offline
    }

    override suspend fun availablePages(): Set<String> = withContext(dispatchers.io) {
        runCatching { override.availablePages() }.getOrDefault(emptySet())
    }

    private suspend fun load(
        pageId: String,
        source: PageDataSource,
        origin: PageOrigin,
        cache: Boolean,
    ): PageLoadResult.Loaded? {
        val startedAt = System.nanoTime()
        val raw = runCatching { source.fetch(pageId) }.getOrNull() ?: return null
        val fetchMillis = (System.nanoTime() - startedAt) / 1_000_000

        val envelope = parse(raw, origin, fetchMillis) ?: return null
        if (cache) payloadCache.write(pageId, raw)
        return PageLoadResult.Loaded(envelope)
    }

    private suspend fun fromCache(pageId: String, reason: StaleReason): PageLoadResult? {
        val cached = payloadCache.read(pageId) ?: return null
        val envelope = parse(cached, PageOrigin.Cache, fetchMillis = 0) ?: return null
        return PageLoadResult.Stale(envelope, reason)
    }

    private fun parse(raw: String, origin: PageOrigin, fetchMillis: Long): PageEnvelope? {
        val (page, parseMillis) = StartupTrace.measured(TRACE_PARSE) {
            runCatching { SduiJson.format.decodeFromString<SduiPage>(raw) }.getOrNull()
        }

        return page?.let {
            PageEnvelope(
                page = it,
                origin = origin,
                fetchMillis = fetchMillis,
                parseMillis = parseMillis,
                payloadBytes = raw.length,
            )
        }
    }

    private companion object {
        const val TRACE_PARSE = "sdui_json_parse"
    }
}
