package com.cars24.sdui.components.section

import com.cars24.sdui.components.SduiComponentType
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cars24.core.designsystem.component.Cars24Card
import com.cars24.core.designsystem.theme.Cars24
import com.cars24.core.designsystem.theme.Radii
import com.cars24.core.designsystem.theme.Spacing
import com.cars24.sdui.runtime.registry.SduiComponent
import com.cars24.sdui.runtime.render.SduiScope
import com.cars24.sdui.runtime.render.rememberProps
import com.cars24.sdui.schema.SduiNode
import kotlinx.serialization.Serializable
import androidx.compose.ui.tooling.preview.Preview
import com.cars24.sdui.components.preview.SduiNodePreview

@Serializable
data class ValueProp(
    val title: String,
    val caption: String? = null,
    val icon: String? = null,
)

@Serializable
data class ValuePropsProps(
    val heading: String? = null,
    val items: List<ValueProp>,
)

class ValuePropsComponent : SduiComponent {
    override val type = SduiComponentType.VALUE_PROPS

    @Composable
    override fun Render(node: SduiNode, scope: SduiScope) {
        val props = rememberProps<ValuePropsProps>(node, scope) ?: return
        if (props.items.isEmpty()) return
        val colors = Cars24.colors

        Cars24Card(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg)) {
            Column(Modifier.padding(Spacing.lg)) {
                if (props.heading != null) {
                    Text(
                        text = props.heading,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                    )
                    Spacer(Modifier.height(Spacing.lg))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    props.items.forEach { item ->
                        Column(
                            modifier = Modifier.weight(1f).padding(horizontal = Spacing.xs),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(Radii.pill)
                                    .background(colors.successContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = valuePropIcon(item.icon),
                                    contentDescription = null,
                                    tint = colors.success,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Spacer(Modifier.height(Spacing.sm))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textPrimary,
                                textAlign = TextAlign.Center,
                            )
                            if (item.caption != null) {
                                Text(
                                    text = item.caption,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textTertiary,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun valuePropIcon(name: String?): ImageVector = when (name) {
    "return" -> Icons.Filled.Refresh
    "paperwork" -> Icons.Filled.List
    "warranty" -> Icons.Filled.Build
    else -> Icons.Filled.CheckCircle
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun ValuePropsComponentPreview() = SduiNodePreview(
    """
    {
      "id": "p", "type": "value_props",
      "props": { "heading": "Every Cars24 car comes with", "items": [
        { "title": "140-point", "caption": "inspection", "icon": "inspection" },
        { "title": "7-day", "caption": "money back", "icon": "return" },
        { "title": "Free RC", "caption": "transfer", "icon": "paperwork" },
        { "title": "1-year", "caption": "warranty", "icon": "warranty" } ] }
    }
    """,
)
