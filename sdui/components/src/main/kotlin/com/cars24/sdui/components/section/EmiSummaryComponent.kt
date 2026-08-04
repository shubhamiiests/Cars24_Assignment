package com.cars24.sdui.components.section

import com.cars24.sdui.components.SduiComponentType
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.cars24.core.designsystem.component.Cars24Button
import com.cars24.core.designsystem.component.Cars24ButtonStyle
import com.cars24.core.designsystem.theme.Cars24
import com.cars24.core.designsystem.theme.Radii
import com.cars24.core.designsystem.theme.Spacing
import com.cars24.sdui.runtime.registry.SduiComponent
import com.cars24.sdui.runtime.render.SduiScope
import com.cars24.sdui.runtime.render.SduiTriggers
import com.cars24.sdui.runtime.render.rememberProps
import com.cars24.sdui.schema.SduiNode
import kotlinx.serialization.Serializable
import androidx.compose.ui.tooling.preview.Preview
import com.cars24.sdui.components.preview.SduiNodePreview

@Serializable
data class EmiRow(val label: String, val value: String)

@Serializable
data class EmiSummaryProps(
    val heading: String,
    val monthly: String,
    val monthlyCaption: String? = null,
    val rows: List<EmiRow> = emptyList(),
    val ctaLabel: String? = null,
)

class EmiSummaryComponent : SduiComponent {
    override val type = SduiComponentType.EMI_SUMMARY

    @Composable
    override fun Render(node: SduiNode, scope: SduiScope) {
        val props = rememberProps<EmiSummaryProps>(node, scope) ?: return
        val colors = Cars24.colors

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg)
                .clip(Radii.lg)
                .background(Brush.horizontalGradient(colors.brandGradient))
                .padding(Spacing.xl),
        ) {
            Text(
                text = props.heading,
                style = MaterialTheme.typography.labelMedium,
                color = colors.onBrandMuted,
            )
            Spacer(Modifier.height(Spacing.xs))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = props.monthly,
                    style = MaterialTheme.typography.displaySmall,
                    color = colors.onBrand,
                )
                if (props.monthlyCaption != null) {
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        text = props.monthlyCaption,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onBrandMuted,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }

            if (props.rows.isNotEmpty()) {
                Spacer(Modifier.height(Spacing.lg))
                props.rows.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.xxs),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = row.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onBrandMuted,
                        )
                        Text(
                            text = row.value,
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.onBrand,
                        )
                    }
                }
            }

            if (props.ctaLabel != null) {
                Spacer(Modifier.height(Spacing.lg))
                Cars24Button(
                    text = props.ctaLabel,
                    onClick = { scope.dispatch(node, SduiTriggers.ON_CLICK) },
                    modifier = Modifier.fillMaxWidth(),
                    style = Cars24ButtonStyle.Accent,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun EmiSummaryComponentPreview() = SduiNodePreview(
    json = """
    {
      "id": "p", "type": "emi_summary",
      "props": {
        "heading": "Your monthly EMI",
        "monthly": "{{state.emi_monthly}}",
        "monthlyCaption": "for {{state.emi_tenure_label}}",
        "rows": [
          { "label": "Total payable", "value": "{{state.emi_total}}" },
          { "label": "Interest rate", "value": "9.7% p.a." }
        ],
        "ctaLabel": "See full breakdown"
      }
    }
    """,
    state = mapOf(
        "emi_monthly" to "Rs 10,780",
        "emi_total" to "Rs 7,76,160",
        "emi_tenure_label" to "72 mo",
    ),
)
