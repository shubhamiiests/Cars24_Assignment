package com.cars24.sdui.components.atom

import com.cars24.sdui.components.SduiComponentType
import com.cars24.sdui.components.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.cars24.core.designsystem.theme.Cars24
import com.cars24.core.designsystem.theme.Spacing
import com.cars24.sdui.runtime.registry.SduiComponent
import com.cars24.sdui.runtime.render.SduiScope
import com.cars24.sdui.runtime.render.SduiTriggers
import com.cars24.sdui.runtime.render.sduiStateValue
import com.cars24.sdui.runtime.render.rememberProps
import com.cars24.sdui.schema.SduiNode
import kotlinx.serialization.Serializable
import androidx.compose.ui.tooling.preview.Preview
import com.cars24.sdui.components.preview.SduiNodePreview

@Serializable
data class ListItemProps(
    val title: String,
    val subtitle: String? = null,
    val trailing: String? = null,
    val icon: String? = null,
    val selectedWhenKey: String? = null,
    val selectedWhenValue: String? = null,
    val showChevron: Boolean = false,
)

class ListItemComponent : SduiComponent {
    override val type = SduiComponentType.LIST_ITEM

    @Composable
    override fun Render(node: SduiNode, scope: SduiScope) {
        val props = rememberProps<ListItemProps>(node, scope) ?: return
        val colors = Cars24.colors

        val selected = props.selectedWhenKey != null &&
            sduiStateValue(props.selectedWhenKey) == props.selectedWhenValue

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { scope.dispatch(node, SduiTriggers.ON_CLICK) }
                .padding(horizontal = Spacing.xl, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val leading = listItemIcon(props.icon)
            if (leading != null) {
                Icon(
                    imageVector = leading,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.primary else colors.textSecondary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(Spacing.lg))
            }

            Column(Modifier.weight(1f)) {
                Text(
                    text = props.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selected) MaterialTheme.colorScheme.primary else colors.textPrimary,
                )
                if (props.subtitle != null) {
                    Text(
                        text = props.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                    )
                }
            }

            if (props.trailing != null) {
                Text(
                    text = props.trailing,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textSecondary,
                )
            }

            when {
                selected -> {
                    Spacer(Modifier.width(Spacing.sm))
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = stringResource(R.string.cmp_cd_selected),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                props.showChevron -> {
                    Spacer(Modifier.width(Spacing.sm))
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = colors.textTertiary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

private fun listItemIcon(name: String?): ImageVector? = when (name) {
    "location" -> Icons.Filled.LocationOn
    "buy" -> Icons.Filled.ShoppingCart
    "sell" -> Icons.Filled.Star
    "lock" -> Icons.Filled.Lock
    "check" -> Icons.Filled.Check
    else -> null
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun ListItemComponentPreview() = SduiNodePreview(
    json = """
    {
      "id": "p", "type": "column", "props": { "spacing": 0 },
      "style": { "margin": { "horizontal": 16 }, "background": "surface", "cornerRadius": 16,
                 "borderWidth": 1, "borderColor": "divider", "padding": { "vertical": 4 } },
      "children": [
        { "id": "l1", "type": "list_item", "props": { "title": "Title only" } },
        { "id": "l2", "type": "list_item", "props": { "title": "With a subtitle", "subtitle": "Explaining the row" } },
        { "id": "l3", "type": "list_item", "props": { "title": "With trailing text", "trailing": "9.7% p.a." } },
        { "id": "l4", "type": "list_item", "props": { "title": "With a chevron", "icon": "buy", "showChevron": true } },
        { "id": "l5", "type": "list_item", "props": { "title": "Mumbai", "icon": "location", "selectedWhenKey": "city", "selectedWhenValue": "Mumbai" } },
        { "id": "l6", "type": "list_item", "props": { "title": "Gurgaon", "icon": "location", "selectedWhenKey": "city", "selectedWhenValue": "Gurgaon" } }
      ]
    }
    """,
    state = mapOf("city" to "Mumbai"),
)
