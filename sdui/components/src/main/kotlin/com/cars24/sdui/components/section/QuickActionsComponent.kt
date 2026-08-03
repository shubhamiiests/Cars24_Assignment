package com.cars24.sdui.components.section

import com.cars24.sdui.components.SduiComponentType
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
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
import com.cars24.sdui.runtime.action.SduiActionParser
import com.cars24.sdui.runtime.registry.SduiComponent
import com.cars24.sdui.runtime.render.SduiScope
import com.cars24.sdui.runtime.render.rememberProps
import com.cars24.sdui.schema.SduiAction
import com.cars24.sdui.schema.SduiNode
import kotlinx.serialization.Serializable

@Serializable
data class QuickAction(
    val label: String,
    val icon: String,
    val caption: String? = null,
    val action: SduiAction? = null,
)

@Serializable
data class QuickActionsProps(val actions: List<QuickAction>)

class QuickActionsComponent : SduiComponent {
    override val type = SduiComponentType.QUICK_ACTIONS

    @Composable
    override fun Render(node: SduiNode, scope: SduiScope) {
        val props = rememberProps<QuickActionsProps>(node, scope) ?: return
        if (props.actions.isEmpty()) return

        Cars24Card(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.lg),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                props.actions.forEach { action ->
                    QuickActionTile(
                        action = action,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            action.action?.let { scope.dispatch(SduiActionParser.parse(it, scope.currentState)) }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    action: QuickAction,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val colors = Cars24.colors

    Column(
        modifier = modifier
            .clip(Radii.md)
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(Radii.md)
                .background(colors.accentContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = iconFor(action.icon),
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = action.label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        if (action.caption != null) {
            Text(
                text = action.caption,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun iconFor(name: String): ImageVector = when (name) {
    "sell" -> Icons.Filled.Star
    "loan", "emi" -> Icons.Filled.DateRange
    "insurance" -> Icons.Filled.Lock
    else -> Icons.Filled.ShoppingCart
}
