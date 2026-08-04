package com.cars24.sdui.components.section

import com.cars24.sdui.components.SduiComponentType
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cars24.core.designsystem.theme.Cars24
import com.cars24.core.designsystem.theme.Radii
import com.cars24.core.designsystem.theme.Spacing
import com.cars24.sdui.runtime.action.SduiActionParser
import com.cars24.sdui.runtime.registry.SduiComponent
import com.cars24.sdui.runtime.render.SduiScope
import com.cars24.sdui.runtime.render.rememberProps
import com.cars24.sdui.runtime.render.resolveColor
import com.cars24.sdui.schema.SduiAction
import com.cars24.sdui.schema.SduiNode
import kotlinx.serialization.Serializable
import androidx.compose.ui.tooling.preview.Preview
import com.cars24.sdui.components.preview.SduiNodePreview

@Serializable
data class BannerSlide(
    val title: String,
    val subtitle: String? = null,
    val ctaLabel: String? = null,
    val gradient: List<String> = emptyList(),
    val action: SduiAction? = null,
)

@Serializable
data class BannerCarouselProps(
    val slides: List<BannerSlide>,
    val height: Int = 150,
)

class BannerCarouselComponent : SduiComponent {
    override val type = SduiComponentType.BANNER_CAROUSEL

    @Composable
    override fun Render(node: SduiNode, scope: SduiScope) {
        val props = rememberProps<BannerCarouselProps>(node, scope) ?: return
        if (props.slides.isEmpty()) return

        val listState = androidx.compose.foundation.lazy.rememberLazyListState()
        val colors = Cars24.colors

        Column {
            LazyRow(
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                contentPadding = PaddingValues(horizontal = Spacing.lg),
            ) {
                items(count = props.slides.size, key = { props.slides[it].title }) { index ->
                    val slide = props.slides[index]
                    BannerSlideCard(
                        slide = slide,
                        height = props.height,
                        onClick = slide.action?.let { action ->
                            { scope.dispatch(SduiActionParser.parse(action, scope.currentState)) }
                        },
                    )
                }
            }

            if (props.slides.size > 1) {
                Spacer(Modifier.height(Spacing.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    repeat(props.slides.size) { index ->
                        val active = index == listState.firstVisibleItemIndex
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(width = if (active) 18.dp else 6.dp, height = 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (active) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        colors.divider
                                    },
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BannerSlideCard(
    slide: BannerSlide,
    height: Int,
    onClick: (() -> Unit)?,
) {
    val colors = Cars24.colors
    val stops = slide.gradient.mapNotNull { resolveColor(it, colors) }
    val brush = if (stops.size >= 2) {
        Brush.horizontalGradient(stops)
    } else {
        Brush.horizontalGradient(colors.brandGradient)
    }

    Column(
        modifier = Modifier
            .width(300.dp)
            .height(height.dp)
            .clip(Radii.lg)
            .background(brush)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(Spacing.xl),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = slide.title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
        )
        if (slide.subtitle != null) {
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = slide.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
        if (slide.ctaLabel != null) {
            Spacer(Modifier.height(Spacing.md))
            Text(
                text = slide.ctaLabel,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                modifier = Modifier
                    .clip(Radii.pill)
                    .background(Color.White.copy(alpha = 0.22f))
                    .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun BannerCarouselComponentPreview() = SduiNodePreview(
    """
    {
      "id": "p", "type": "banner_carousel",
      "props": { "height": 150, "slides": [
        { "title": "Zero down payment", "subtitle": "On 2,000+ assured cars this month",
          "ctaLabel": "Check eligibility", "gradient": ["#1B2065", "#5865C4"] },
        { "title": "Sell in a single visit", "subtitle": "Instant payment, free RC transfer",
          "ctaLabel": "Get a quote", "gradient": ["#0B8A6B", "#3FCFA8"] } ] }
    }
    """,
)
