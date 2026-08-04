package com.cars24.feature.sduipage

import androidx.compose.ui.res.stringResource
import com.cars24.data.page.PageFailure
import com.cars24.data.page.StaleReason
import com.cars24.feature.sduipage.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cars24.core.designsystem.component.ErrorState
import com.cars24.core.designsystem.component.OfflineState
import com.cars24.core.designsystem.component.PageSkeleton
import com.cars24.core.designsystem.component.StaleBanner
import com.cars24.core.designsystem.theme.Cars24
import com.cars24.sdui.components.Cars24Components
import com.cars24.sdui.runtime.render.LocalSduiPageState
import com.cars24.sdui.runtime.render.SduiChildren
import com.cars24.sdui.runtime.render.SduiPageHost
import com.cars24.sdui.runtime.render.SduiScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SduiPageScreen(
    state: PageUiState,
    onIntent: (PageIntent) -> Unit,
    showBackButton: Boolean = false,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val registry = remember { Cars24Components.registry() }
    val listState = rememberLazyListState()

    val latestState = rememberUpdatedState(state.pageState)
    val latestOnIntent = rememberUpdatedState(onIntent)
    val scope = remember(registry) {
        SduiScope(
            registry = registry,
            pageSchemaVersion = 1,
            showUnknownPlaceholders = true,
            stateProvider = { latestState.value },
            onCommand = { latestOnIntent.value(PageIntent.Command(it)) },
            onUnsupportedType = { latestOnIntent.value(PageIntent.UnsupportedComponent(it)) },
        )
    }

    LaunchedEffect(state.page?.pageId, state.scrollIndex) {
        if (state.page != null && state.scrollIndex > 0 && listState.firstVisibleItemIndex == 0) {
            listState.scrollToItem(state.scrollIndex, state.scrollOffset)
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .debounce(SCROLL_PERSIST_DEBOUNCE_MS)
            .collect { (index, offset) -> onIntent(PageIntent.ScrollChanged(index, offset)) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (state.phase) {
            PagePhase.Loading -> PageSkeleton(Modifier.statusBarsPadding())

            PagePhase.Offline -> Column(Modifier.statusBarsPadding()) {
                if (showBackButton) PageTopBar(title = null, onBack = onBack)
                OfflineState(onRetry = { onIntent(PageIntent.Retry) })
            }

            PagePhase.Failed -> Column(Modifier.statusBarsPadding()) {
                if (showBackButton) PageTopBar(title = null, onBack = onBack)
                ErrorState(
                    onRetry = { onIntent(PageIntent.Retry) },
                    message = state.failure.toMessage(),
                )
            }

            PagePhase.Content -> {
                val page = state.page ?: return@Box
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { onIntent(PageIntent.Refresh) },
                ) {
                SduiPageHost(
                    page = page,
                    scope = scope,
                    pageState = state.pageState,
                    listState = listState,
                    contentPadding = PaddingValues(bottom = 24.dp),
                    header = {
                        Column(Modifier.statusBarsPadding()) {
                            if (showBackButton) {
                                PageTopBar(title = page.title, onBack = onBack)
                            }
                            val staleReason = state.staleReason
                            if (staleReason != null) {
                                StaleBanner(
                                    text = staleReason.toMessage(),
                                    onRetry = { onIntent(PageIntent.Retry) },
                                )
                            }
                        }
                    },
                )
                }
            }
        }
    }

    val sheet = state.openSheet
    if (sheet != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { onIntent(PageIntent.DismissSheet) },
            sheetState = sheetState,
            containerColor = Cars24.colors.cardSurface,
        ) {
            Column(Modifier.navigationBarsPadding()) {
                CompositionLocalProvider(LocalSduiPageState provides state.pageState) {
                    SduiChildren(sheet.content, scope)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun StaleReason.toMessage(): String = stringResource(
    when (this) {
        StaleReason.NoConnection -> R.string.page_stale_no_connection
        StaleReason.ServerPayloadUnusable -> R.string.page_stale_server_unusable
    },
)

@Composable
private fun PageFailure?.toMessage(): String = stringResource(
    when (this) {
        PageFailure.PushedPayloadUnparseable -> R.string.page_failed_pushed_payload
        else -> R.string.page_failed_server_payload
    },
)

@Composable
private fun PageTopBar(title: String?, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Cars24.colors.cardSurface)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = stringResource(R.string.page_cd_back),
                tint = Cars24.colors.textPrimary,
            )
        }
        Text(
            text = title.orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            color = Cars24.colors.textPrimary,
        )
    }
}

private const val SCROLL_PERSIST_DEBOUNCE_MS = 300L
