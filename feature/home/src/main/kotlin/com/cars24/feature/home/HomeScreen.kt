package com.cars24.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cars24.core.designsystem.component.ErrorState
import com.cars24.core.designsystem.component.OfflineState
import com.cars24.core.designsystem.component.PageSkeleton
import com.cars24.core.designsystem.component.StaleBanner
import com.cars24.core.designsystem.theme.Cars24
import com.cars24.sdui.components.Cars24Components
import com.cars24.sdui.runtime.render.SduiChildren
import com.cars24.sdui.runtime.render.SduiPageHost
import com.cars24.sdui.runtime.render.SduiScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val registry = remember { Cars24Components.registry() }
    val listState = rememberLazyListState()

    val scope = remember(state.pageState, registry) {
        SduiScope(
            registry = registry,
            state = state.pageState,
            pageSchemaVersion = state.page?.schemaVersion ?: 1,
            showUnknownPlaceholders = true,
            onCommand = { onIntent(HomeIntent.Command(it)) },
            onUnsupportedType = { onIntent(HomeIntent.UnsupportedComponent(it)) },
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
            .collect { (index, offset) -> onIntent(HomeIntent.ScrollChanged(index, offset)) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (state.phase) {
            HomePhase.Loading -> PageSkeleton(Modifier.statusBarsPadding())

            HomePhase.Offline -> OfflineState(onRetry = { onIntent(HomeIntent.Retry) })

            HomePhase.Failed -> ErrorState(
                message = state.failureMessage ?: "We could not read the page layout.",
                onRetry = { onIntent(HomeIntent.Retry) },
            )

            HomePhase.Content -> {
                val page = state.page ?: return@Box
                SduiPageHost(
                    page = page,
                    scope = scope,
                    listState = listState,
                    contentPadding = PaddingValues(bottom = 24.dp),
                    header = {
                        Column(Modifier.statusBarsPadding()) {
                            if (state.staleMessage != null) {
                                StaleBanner(
                                    text = state.staleMessage,
                                    onRetry = { onIntent(HomeIntent.Retry) },
                                )
                            }
                        }
                    },
                )
            }
        }
    }

    val sheet = state.openSheet
    if (sheet != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { onIntent(HomeIntent.DismissSheet) },
            sheetState = sheetState,
            containerColor = Cars24.colors.cardSurface,
        ) {
            Column(Modifier.navigationBarsPadding()) {
                SduiChildren(sheet.content, scope)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

private const val SCROLL_PERSIST_DEBOUNCE_MS = 300L
