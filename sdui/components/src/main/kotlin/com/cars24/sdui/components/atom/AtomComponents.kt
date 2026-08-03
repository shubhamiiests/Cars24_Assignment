package com.cars24.sdui.components.atom

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
import com.cars24.sdui.runtime.render.rememberProps
import com.cars24.sdui.runtime.render.resolveColor
import com.cars24.sdui.schema.SduiAction
import com.cars24.sdui.schema.SduiNode
import kotlinx.serialization.Serializable

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
    override val type = "text"

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
    override val type = "image"

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
    override val type = "button"

    @Composable
    override fun Render(node: SduiNode, scope: SduiScope) {
        val props = rememberProps<ButtonProps>(node, scope) ?: return

        Cars24Button(
            text = props.label,
            onClick = { scope.dispatch(node, SduiTriggers.ON_CLICK) },
            modifier = if (props.fillWidth) Modifier.fillMaxWidth() else Modifier,
            style = when (props.variant) {
                "accent" -> Cars24ButtonStyle.Accent
                "outline" -> Cars24ButtonStyle.Outline
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
    override val type = "chip_group"

    @Composable
    override fun Render(node: SduiNode, scope: SduiScope) {
        val props = rememberProps<ChipGroupProps>(node, scope) ?: return
        val selected = scope.state[props.stateKey]

        val onSelect: (ChipOption) -> Unit = { option ->
            val command = option.action
                ?.let { SduiActionParser.parse(it, scope.state) }
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
    override val type = "tag_row"

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
