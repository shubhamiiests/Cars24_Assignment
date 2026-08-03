package com.cars24.sdui.components.layout

import com.cars24.sdui.components.SduiComponentType
import com.cars24.sdui.components.LayoutToken
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cars24.core.designsystem.theme.Cars24
import com.cars24.sdui.components.horizontalAlignmentToken
import com.cars24.sdui.components.verticalAlignmentToken
import com.cars24.sdui.runtime.registry.SduiComponent
import com.cars24.sdui.runtime.render.SduiChildren
import com.cars24.sdui.runtime.render.SduiNodeRenderer
import com.cars24.sdui.runtime.render.SduiScope
import com.cars24.sdui.runtime.render.rememberProps
import com.cars24.sdui.schema.SduiNode
import kotlinx.serialization.Serializable

@Serializable
data class StackProps(
    val spacing: Int = 0,
    val align: String? = null,
    val arrangement: String? = null,
)

class ColumnComponent : SduiComponent {
    override val type = SduiComponentType.COLUMN

    @Composable
    override fun Render(node: SduiNode, scope: SduiScope) {
        val props = rememberProps<StackProps>(node, scope) ?: StackProps()
        Column(
            verticalArrangement = Arrangement.spacedBy(props.spacing.dp),
            horizontalAlignment = horizontalAlignmentToken(props.align),
        ) {
            SduiChildren(node.children, scope)
        }
    }
}

class RowComponent : SduiComponent {
    override val type = SduiComponentType.ROW

    @Composable
    override fun Render(node: SduiNode, scope: SduiScope) {
        val props = rememberProps<StackProps>(node, scope) ?: StackProps()
        Row(
            horizontalArrangement = when (props.arrangement) {
                LayoutToken.CENTER -> Arrangement.Center
                LayoutToken.END -> Arrangement.End
                LayoutToken.SPACE_BETWEEN -> Arrangement.SpaceBetween
                LayoutToken.SPACE_AROUND -> Arrangement.SpaceAround
                else -> Arrangement.spacedBy(props.spacing.dp)
            },
            verticalAlignment = verticalAlignmentToken(props.align),
        ) {
            SduiChildren(node.children, scope)
        }
    }
}

@Serializable
data class CarouselProps(
    val itemSpacing: Int = 12,
    val startPadding: Int = 16,
    val endPadding: Int = 16,
)

class CarouselComponent : SduiComponent {
    override val type = SduiComponentType.CAROUSEL

    @Composable
    override fun Render(node: SduiNode, scope: SduiScope) {
        val props = rememberProps<CarouselProps>(node, scope) ?: CarouselProps()
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(props.itemSpacing.dp),
            contentPadding = PaddingValues(start = props.startPadding.dp, end = props.endPadding.dp),
        ) {
            items(
                count = node.children.size,
                key = { index -> node.children[index].id },
            ) { index ->
                SduiNodeRenderer(node = node.children[index], scope = scope)
            }
        }
    }
}

@Serializable
data class GridProps(
    val columns: Int = 2,
    val itemSpacing: Int = 12,
    val rowSpacing: Int = 12,
)

class GridComponent : SduiComponent {
    override val type = SduiComponentType.GRID

    @Composable
    override fun Render(node: SduiNode, scope: SduiScope) {
        val props = rememberProps<GridProps>(node, scope) ?: GridProps()
        val columns = props.columns.coerceAtLeast(1)

        Column(verticalArrangement = Arrangement.spacedBy(props.rowSpacing.dp)) {
            node.children.chunked(columns).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(props.itemSpacing.dp),
                ) {
                    rowItems.forEach { child ->
                        Column(modifier = Modifier.weight(1f)) {
                            SduiNodeRenderer(node = child, scope = scope)
                        }
                    }
                    repeat(columns - rowItems.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Serializable
data class SpacerProps(val size: Int = 8)

class SpacerComponent : SduiComponent {
    override val type = SduiComponentType.SPACER

    @Composable
    override fun Render(node: SduiNode, scope: SduiScope) {
        val props = rememberProps<SpacerProps>(node, scope) ?: SpacerProps()
        Spacer(Modifier.size(props.size.dp))
    }
}

@Serializable
data class DividerProps(
    val thickness: Int = 1,
    val insetStart: Int = 0,
    val insetEnd: Int = 0,
)

class DividerComponent : SduiComponent {
    override val type = SduiComponentType.DIVIDER

    @Composable
    override fun Render(node: SduiNode, scope: SduiScope) {
        val props = rememberProps<DividerProps>(node, scope) ?: DividerProps()
        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(props.insetStart.dp))
            HorizontalDivider(
                modifier = Modifier
                    .weight(1f)
                    .height(props.thickness.dp),
                color = Cars24.colors.divider,
            )
            Spacer(Modifier.width(props.insetEnd.dp))
        }
    }
}
