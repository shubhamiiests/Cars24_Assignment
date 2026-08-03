package com.cars24.sdui.runtime.render

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.cars24.core.common.perf.StartupTrace
import com.cars24.core.designsystem.theme.Cars24
import com.cars24.sdui.schema.SduiNode
import com.cars24.sdui.schema.SduiPage
import kotlinx.coroutines.flow.first

@Composable
fun SduiPageHost(
    page: SduiPage,
    scope: SduiScope,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(),
    header: @Composable () -> Unit = {},
) {
    val background = page.background
        ?.let { resolveColor(it, Cars24.colors) }
        ?: Cars24.colors.pageBackground

    val visibleSections = page.sections.filter { section ->
        section.visibleWhen?.evaluate(scope.state) != false
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(background),
        state = listState,
        contentPadding = contentPadding,
    ) {
        item(key = "sdui_header") { header() }

        items(items = visibleSections, key = SduiNode::id) { section ->
            SduiNodeRenderer(node = section, scope = scope)
        }
    }

    RecordRenderMarks(listState = listState, sectionCount = visibleSections.size)

    ReportDrawnWhen { visibleSections.isNotEmpty() }
}


@Composable
private fun RecordRenderMarks(listState: LazyListState, sectionCount: Int) {
    LaunchedEffect(sectionCount) {
        if (sectionCount == 0) return@LaunchedEffect

        snapshotFlow { listState.layoutInfo.visibleItemsInfo.isNotEmpty() }.first { it }
        StartupTrace.mark(StartupTrace.MARK_FIRST_SECTION_DRAWN)
        StartupTrace.mark(StartupTrace.MARK_INTERACTIVE)

        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
            .first { it >= sectionCount }
        StartupTrace.mark(StartupTrace.MARK_FULL_PAGE)
    }
}
