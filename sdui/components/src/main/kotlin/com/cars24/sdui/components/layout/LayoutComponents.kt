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
import androidx.compose.ui.tooling.preview.Preview
import com.cars24.sdui.components.preview.SduiNodePreview

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

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun ColumnComponentPreview() = SduiNodePreview(
    """
    {
      "id": "p", "type": "column", "props": { "spacing": 8 },
      "style": { "margin": { "horizontal": 16 }, "padding": { "all": 16 },
                 "background": "surface", "cornerRadius": 16,
                 "borderWidth": 1, "borderColor": "divider" },
      "children": [
        { "id": "a", "type": "text", "props": { "value": "A styled column", "style": "title_medium" } },
        { "id": "b", "type": "text", "props": { "value": "Padding, background, corners and border come from the universal style block.", "style": "body_small", "color": "text_secondary" } }
      ]
    }
    """,
)

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun RowComponentPreview() = SduiNodePreview(
    """
    {
      "id": "p", "type": "row",
      "props": { "arrangement": "space_between", "align": "center" },
      "style": { "padding": { "horizontal": 16 } },
      "children": [
        { "id": "l", "type": "text", "props": { "value": "Total payable", "style": "body_small", "color": "text_secondary" } },
        { "id": "r", "type": "text", "props": { "value": "Rs 7,11,360", "style": "label_large" } }
      ]
    }
    """,
)

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun CarouselComponentPreview() = SduiNodePreview(
    """
    {
      "id": "p", "type": "carousel", "props": { "itemSpacing": 12 },
      "children": [
        { "id": "c1", "type": "car_card", "props": { "name": "Maruti Swift VXi", "price": "Rs 5.24 L", "emi": "Rs 11,400/mo", "specs": ["2019", "42,150 km", "Petrol"], "savings": "Save 38k", "assured": true, "wishKey": "w1" } },
        { "id": "c2", "type": "car_card", "props": { "name": "Hyundai i20 Sportz", "price": "Rs 6.85 L", "emi": "Rs 14,820/mo", "specs": ["2020", "31,900 km", "Petrol"], "badge": "Popular", "assured": true, "wishKey": "w2" } }
      ]
    }
    """,
)

@Preview(showBackground = true, widthDp = 380, heightDp = 600)
@Composable
private fun GridComponentPreview() = SduiNodePreview(
    """
    {
      "id": "p", "type": "grid", "props": { "columns": 2, "itemSpacing": 12, "rowSpacing": 12 },
      "style": { "padding": { "horizontal": 16 } },
      "children": [
        { "id": "g1", "type": "car_card", "props": { "name": "Hyundai Creta SX", "price": "Rs 11.40 L", "emi": "Rs 24,650/mo", "specs": ["2020", "44,100 km"], "assured": true, "fillWidth": true, "wishKey": "w1" } },
        { "id": "g2", "type": "car_card", "props": { "name": "Hyundai Venue S", "price": "Rs 8.20 L", "emi": "Rs 17,740/mo", "specs": ["2021", "29,600 km"], "badge": "New", "fillWidth": true, "wishKey": "w2" } },
        { "id": "g3", "type": "car_card", "props": { "name": "Maruti Brezza ZXi", "price": "Rs 9.65 L", "emi": "Rs 20,880/mo", "specs": ["2020", "38,500 km"], "assured": true, "fillWidth": true, "wishKey": "w3" } }
      ]
    }
    """,
)

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun SpacerAndDividerPreview() = SduiNodePreview(
    """
    {
      "id": "p", "type": "column", "props": { "spacing": 0 },
      "children": [
        { "id": "t1", "type": "text", "props": { "value": "Above" }, "style": { "padding": { "horizontal": 16 } } },
        { "id": "s1", "type": "spacer", "props": { "size": 24 } },
        { "id": "d", "type": "divider", "props": { "thickness": 1, "insetStart": 16, "insetEnd": 16 } },
        { "id": "s2", "type": "spacer", "props": { "size": 24 } },
        { "id": "t2", "type": "text", "props": { "value": "Below" }, "style": { "padding": { "horizontal": 16 } } }
      ]
    }
    """,
)
