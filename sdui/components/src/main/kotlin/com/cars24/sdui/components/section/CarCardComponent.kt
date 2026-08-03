package com.cars24.sdui.components.section

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cars24.core.designsystem.component.Cars24Card
import com.cars24.core.designsystem.component.Cars24Tag
import com.cars24.core.designsystem.component.gradientFor
import com.cars24.core.designsystem.theme.Cars24
import com.cars24.core.designsystem.theme.PriceTextStyle
import com.cars24.core.designsystem.theme.Radii
import com.cars24.core.designsystem.theme.Spacing
import com.cars24.sdui.runtime.registry.SduiComponent
import com.cars24.sdui.runtime.render.SduiScope
import com.cars24.sdui.runtime.render.SduiTriggers
import com.cars24.sdui.runtime.render.rememberProps
import com.cars24.sdui.schema.SduiNode
import kotlinx.serialization.Serializable

@Serializable
data class CarCardProps(
    val name: String,
    val price: String,
    val emi: String? = null,
    val specs: List<String> = emptyList(),
    val badge: String? = null,
    val savings: String? = null,
    val assured: Boolean = false,
    val layout: String = "vertical",
    val width: Int = 220,
)

class CarCardComponent : SduiComponent {
    override val type = "car_card"

    @Composable
    override fun Render(node: SduiNode, scope: SduiScope) {
        val props = rememberProps<CarCardProps>(node, scope) ?: return
        val horizontal = props.layout == "horizontal"

        Cars24Card(
            modifier = Modifier
                .then(if (horizontal) Modifier.fillMaxWidth() else Modifier.width(props.width.dp))
                .clickable { scope.dispatch(node, SduiTriggers.ON_CLICK) },
        ) {
            if (horizontal) {
                Row(Modifier.padding(Spacing.md)) {
                    CarThumbnail(
                        props = props,
                        modifier = Modifier
                            .width(120.dp)
                            .height(90.dp),
                    )
                    Spacer(Modifier.width(Spacing.md))
                    Column(Modifier.weight(1f)) { CarDetails(props) }
                }
            } else {
                Column {
                    CarThumbnail(
                        props = props,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                    )
                    Column(Modifier.padding(Spacing.md)) { CarDetails(props) }
                }
            }
        }
    }
}

@Composable
private fun CarThumbnail(props: CarCardProps, modifier: Modifier) {
    Box(modifier = modifier.background(gradientFor(props.name), RoundedCornerShape(12.dp))) {
        if (props.badge != null) {
            Surface(
                modifier = Modifier.padding(Spacing.sm),
                shape = Radii.sm,
                color = Color.White.copy(alpha = 0.92f),
            ) {
                Text(
                    text = props.badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = Cars24.colors.textPrimary,
                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
                )
            }
        }

        Icon(
            imageVector = Icons.Filled.FavoriteBorder,
            contentDescription = "Save",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(Spacing.sm)
                .size(20.dp),
        )
    }
}

@Composable
private fun CarDetails(props: CarCardProps) {
    val colors = Cars24.colors

    if (props.specs.isNotEmpty()) {
        Text(
            text = props.specs.joinToString(" • "),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(Spacing.xxs))
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = props.name,
            style = MaterialTheme.typography.titleSmall,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (props.assured) {
            Spacer(Modifier.width(Spacing.xs))
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Cars24 assured",
                tint = colors.success,
                modifier = Modifier.size(14.dp),
            )
        }
    }

    Spacer(Modifier.height(Spacing.sm))

    Row(verticalAlignment = Alignment.Bottom) {
        Text(text = props.price, style = PriceTextStyle, color = colors.price)
        if (props.savings != null) {
            Spacer(Modifier.width(Spacing.sm))
            Text(
                text = props.savings,
                style = MaterialTheme.typography.labelSmall,
                color = colors.success,
                modifier = Modifier.padding(bottom = 2.dp),
            )
        }
    }

    if (props.emi != null) {
        Spacer(Modifier.height(Spacing.xs))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Cars24Tag(text = props.emi)
        }
    }
}
