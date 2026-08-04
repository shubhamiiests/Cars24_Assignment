package com.cars24.sdui.components.atom

import com.cars24.sdui.components.SduiComponentType
import com.cars24.sdui.components.ButtonVariant
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cars24.core.designsystem.component.Cars24Button
import com.cars24.core.designsystem.component.Cars24ButtonStyle
import com.cars24.core.designsystem.component.Cars24Chip
import com.cars24.core.designsystem.component.Cars24Tag
import com.cars24.core.designsystem.component.NetworkImage
import com.cars24.core.designsystem.theme.Cars24
import com.cars24.sdui.components.fontWeightToken
import com.cars24.sdui.components.textAlignToken
import com.cars24.sdui.components.textStyleToken
import com.cars24.sdui.runtime.action.SduiActionParser
import com.cars24.sdui.runtime.action.SduiCommand
import com.cars24.sdui.runtime.registry.SduiComponent
import com.cars24.sdui.runtime.render.SduiScope
import com.cars24.sdui.runtime.render.SduiTriggers
import com.cars24.sdui.runtime.render.sduiStateValue
import com.cars24.sdui.runtime.render.rememberProps
import com.cars24.sdui.runtime.render.resolveColor
import com.cars24.sdui.schema.SduiAction
import com.cars24.sdui.schema.SduiNode
import kotlinx.serialization.Serializable
import androidx.compose.ui.tooling.preview.Preview
import com.cars24.sdui.components.preview.SduiNodePreview

@Serializable
data class TextProps(
    val value: String,
    val style: String? = null,
    val color: String? = null,
    val align: String? = null,
    val weight: String? = null,
    val maxLines: Int = Int.MAX_VALUE,
)

class TextComponent : SduiComponent {
    override val type = SduiComponentType.TEXT

    @Composable
    override fun Render(node: SduiNode, scope: SduiScope) {
        val props = rememberProps<TextProps>(node, scope) ?: return
        val colors = Cars24.colors

        Text(
            text = props.value,
            style = textStyleToken(props.style),
            color = props.color?.let { resolveColor(it, colors) } ?: colors.textPrimary,
            textAlign = textAlignToken(props.align),
            fontWeight = fontWeightToken(props.weight),
            maxLines = props.maxLines,
        )
    }
}

@Serializable
data class ImageProps(
    val seed: String,
    val url: String? = null,
    val cornerRadius: Int = 12,
    val height: Int? = null,
)

class ImageComponent : SduiComponent {
    override val type = SduiComponentType.IMAGE

    @Composable
    override fun Render(node: SduiNode, scope: SduiScope) {
        val props = rememberProps<ImageProps>(node, scope) ?: return
        NetworkImage(
            url = props.url,
            seed = props.seed,
            shape = RoundedCornerShape(props.cornerRadius.dp),
            modifier = Modifier
                .fillMaxWidth()
                .then(props.height?.let { Modifier.height(it.dp) } ?: Modifier),
        )
    }
}

@Serializable
data class ButtonProps(
    val label: String,
    val variant: String? = null,
    val fillWidth: Boolean = false,
)

class ButtonComponent : SduiComponent {
    override val type = SduiComponentType.BUTTON

    @Composable
    override fun Render(node: SduiNode, scope: SduiScope) {
        val props = rememberProps<ButtonProps>(node, scope) ?: return

        Cars24Button(
            text = props.label,
            onClick = { scope.dispatch(node, SduiTriggers.ON_CLICK) },
            modifier = if (props.fillWidth) Modifier.fillMaxWidth() else Modifier,
            style = when (props.variant) {
                ButtonVariant.ACCENT -> Cars24ButtonStyle.Accent
                ButtonVariant.OUTLINE -> Cars24ButtonStyle.Outline
                else -> Cars24ButtonStyle.Primary
            },
        )
    }
}

@Serializable
data class ChipOption(
    val label: String,
    val value: String,
    val supporting: String? = null,
    val action: SduiAction? = null,
)

@Serializable
data class ChipGroupProps(
    val stateKey: String,
    val options: List<ChipOption>,
    val scrollable: Boolean = true,
)

class ChipGroupComponent : SduiComponent {
    override val type = SduiComponentType.CHIP_GROUP

    @Composable
    override fun Render(node: SduiNode, scope: SduiScope) {
        val props = rememberProps<ChipGroupProps>(node, scope) ?: return
        val selected = sduiStateValue(props.stateKey)

        val onSelect: (ChipOption) -> Unit = { option ->
            val command = option.action
                ?.let { SduiActionParser.parse(it, scope.currentState) }
                ?: SduiCommand.SetState(props.stateKey, option.value)
            scope.dispatch(command)
            scope.dispatch(node, SduiTriggers.ON_SELECT)
        }

        if (props.scrollable) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                items(count = props.options.size, key = { props.options[it].value }) { index ->
                    val option = props.options[index]
                    Cars24Chip(
                        label = option.label,
                        selected = option.value == selected,
                        onClick = { onSelect(option) },
                        supporting = option.supporting,
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                props.options.forEach { option ->
                    Cars24Chip(
                        label = option.label,
                        selected = option.value == selected,
                        onClick = { onSelect(option) },
                        supporting = option.supporting,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Serializable
data class TagRowProps(
    val tags: List<String>,
    val emphasisedFirst: Boolean = false,
)

class TagRowComponent : SduiComponent {
    override val type = SduiComponentType.TAG_ROW

    @Composable
    override fun Render(node: SduiNode, scope: SduiScope) {
        val props = rememberProps<TagRowProps>(node, scope) ?: return
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            props.tags.forEachIndexed { index, tag ->
                Cars24Tag(text = tag, emphasised = props.emphasisedFirst && index == 0)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380)
@Preview(showBackground = true, widthDp = 380, uiMode = 0x20)
@Composable
private fun TextComponentPreview() = SduiNodePreview(
    """
    {
      "id": "p", "type": "column", "props": { "spacing": 6 },
      "style": { "padding": { "horizontal": 16 } },
      "children": [
        { "id": "t1", "type": "text", "props": { "value": "display_small", "style": "display_small" } },
        { "id": "t2", "type": "text", "props": { "value": "headline_small", "style": "headline_small" } },
        { "id": "t3", "type": "text", "props": { "value": "title_large", "style": "title_large" } },
        { "id": "t4", "type": "text", "props": { "value": "body_medium", "style": "body_medium" } },
        { "id": "t5", "type": "text", "props": { "value": "label_small on a token colour", "style": "label_small", "color": "text_tertiary" } },
        { "id": "t6", "type": "text", "props": { "value": "Rs 5.24 L", "style": "price", "color": "price" } },
        { "id": "t7", "type": "text", "props": { "value": "accent, bold, centred", "style": "title_medium", "color": "accent", "weight": "bold", "align": "center" } }
      ]
    }
    """,
)

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun ImageComponentPreview() = SduiNodePreview(
    """
    {
      "id": "p", "type": "image",
      "props": { "seed": "Maruti Swift VXi", "cornerRadius": 16, "height": 160 },
      "style": { "margin": { "horizontal": 16 } }
    }
    """,
)

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun ButtonComponentPreview() = SduiNodePreview(
    """
    {
      "id": "p", "type": "column", "props": { "spacing": 10 },
      "style": { "padding": { "horizontal": 16 } },
      "children": [
        { "id": "b1", "type": "button", "props": { "label": "Primary", "fillWidth": true } },
        { "id": "b2", "type": "button", "props": { "label": "Accent", "variant": "accent", "fillWidth": true } },
        { "id": "b3", "type": "button", "props": { "label": "Outline", "variant": "outline", "fillWidth": true } }
      ]
    }
    """,
)

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun ChipGroupComponentPreview() = SduiNodePreview(
    json = """
    {
      "id": "p", "type": "chip_group",
      "props": { "stateKey": "fuel", "options": [
        { "label": "All", "value": "all", "supporting": "412" },
        { "label": "Petrol", "value": "petrol", "supporting": "268" },
        { "label": "Diesel", "value": "diesel", "supporting": "91" },
        { "label": "CNG", "value": "cng", "supporting": "53" } ] }
    }
    """,
    state = mapOf("fuel" to "diesel"),
)

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun ChipGroupFixedWidthPreview() = SduiNodePreview(
    json = """
    {
      "id": "p", "type": "chip_group",
      "props": { "stateKey": "tenure", "scrollable": false, "options": [
        { "label": "36 mo", "value": "36" }, { "label": "48 mo", "value": "48" },
        { "label": "60 mo", "value": "60" }, { "label": "72 mo", "value": "72" } ] },
      "style": { "padding": { "horizontal": 16 } }
    }
    """,
    state = mapOf("tenure" to "48"),
)

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun TagRowComponentPreview() = SduiNodePreview(
    """
    {
      "id": "p", "type": "tag_row",
      "props": { "tags": ["Cars24 Assured", "Fixed price", "Free RC transfer"], "emphasisedFirst": true },
      "style": { "padding": { "horizontal": 16 } }
    }
    """,
)
