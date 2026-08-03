package com.cars24.sdui.runtime.render

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.cars24.sdui.schema.SduiJson
import com.cars24.sdui.schema.SduiNode

@Composable
fun SduiNodeRenderer(
    node: SduiNode,
    scope: SduiScope,
    modifier: Modifier = Modifier,
) {
    val condition = node.visibleWhen
    if (condition != null && !condition.evaluate(LocalSduiPageState.current)) return

    if (node.minSchemaVersion > SduiJson.SUPPORTED_SCHEMA_VERSION) {
        SduiLog.tooNew(node, SduiJson.SUPPORTED_SCHEMA_VERSION)
        RenderDegraded(node, scope, modifier, DegradeReason.SchemaTooNew)
        return
    }

    val component = scope.registry.find(node.type)
    if (component == null) {
        SduiLog.unsupportedType(node)
        RenderDegraded(node, scope, modifier, DegradeReason.UnknownType)
        return
    }

    if (scope.hasAction(node, SduiTriggers.ON_APPEAR)) {
        LaunchedEffect(node.id) { scope.dispatch(node, SduiTriggers.ON_APPEAR) }
    }

    Box(modifier = modifier.then(node.style.toModifier())) {
        component.Render(node, scope)
    }
}

internal enum class DegradeReason { UnknownType, SchemaTooNew }

@Composable
private fun RenderDegraded(
    node: SduiNode,
    scope: SduiScope,
    modifier: Modifier,
    reason: DegradeReason,
) {
    scope.reportUnsupportedType(node.type)

    val fallback = node.fallback
    if (fallback != null && scope.registry.find(fallback.type) != null) {
        SduiNodeRenderer(node = fallback.copy(fallback = null), scope = scope, modifier = modifier)
        return
    }

    if (scope.showUnknownPlaceholders) {
        UnsupportedSectionPlaceholder(
            type = node.type,
            reason = reason,
            modifier = modifier.then(node.style.toModifier()),
        )
    }
}


@Composable
fun SduiChildren(
    nodes: List<SduiNode>,
    scope: SduiScope,
    modifier: Modifier = Modifier,
) {
    nodes.forEach { child ->
        SduiNodeRenderer(node = child, scope = scope, modifier = modifier)
    }
}
