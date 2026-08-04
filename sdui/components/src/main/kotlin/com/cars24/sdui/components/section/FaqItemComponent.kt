package com.cars24.sdui.components.section

import com.cars24.sdui.components.SduiComponentType
import com.cars24.sdui.components.R
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.cars24.core.designsystem.component.Cars24Card
import com.cars24.core.designsystem.theme.Cars24
import com.cars24.core.designsystem.theme.Spacing
import com.cars24.sdui.runtime.registry.SduiComponent
import com.cars24.sdui.runtime.render.SduiScope
import com.cars24.sdui.runtime.render.rememberProps
import com.cars24.sdui.schema.SduiNode
import kotlinx.serialization.Serializable
import androidx.compose.ui.tooling.preview.Preview
import com.cars24.sdui.components.preview.SduiNodePreview

@Serializable
data class FaqItemProps(
    val question: String,
    val answer: String,
    val startExpanded: Boolean = false,
)

class FaqItemComponent : SduiComponent {
    override val type = SduiComponentType.FAQ_ITEM

    @Composable
    override fun Render(node: SduiNode, scope: SduiScope) {
        val props = rememberProps<FaqItemProps>(node, scope) ?: return
        var expanded by rememberSaveable(node.id) { mutableStateOf(props.startExpanded) }
        val rotation by animateFloatAsState(
            targetValue = if (expanded) 180f else 0f,
            label = "faqChevron",
        )
        val colors = Cars24.colors

        Cars24Card(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg)) {
            Column(
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(Spacing.lg),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = props.question,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = stringResource(
                            if (expanded) R.string.cmp_cd_collapse else R.string.cmp_cd_expand,
                        ),
                        tint = colors.textSecondary,
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(rotation),
                    )
                }

                AnimatedVisibility(visible = expanded) {
                    Column {
                        Spacer(Modifier.height(Spacing.sm))
                        Text(
                            text = props.answer,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun FaqItemComponentPreview() = SduiNodePreview(
    """
    {
      "id": "p", "type": "column", "props": { "spacing": 8 },
      "children": [
        { "id": "f1", "type": "faq_item", "props": { "question": "How does the 7-day money back work?", "answer": "Drive the car for up to 7 days or 350 km. If it is not right for you, return it at any Cars24 hub and we refund the full amount.", "startExpanded": true } },
        { "id": "f2", "type": "faq_item", "props": { "question": "Is the RC transfer really free?", "answer": "Yes. We handle the paperwork end to end and absorb the transfer fee." } }
      ]
    }
    """,
)
