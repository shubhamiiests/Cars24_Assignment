package com.cars24.core.designsystem.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import com.cars24.core.designsystem.theme.Cars24
import com.cars24.core.designsystem.theme.Radii
import androidx.compose.ui.tooling.preview.Preview
import com.cars24.core.designsystem.theme.Cars24Theme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import com.cars24.core.designsystem.theme.Spacing


@Composable
fun ShimmerBlock(
    modifier: Modifier = Modifier,
    shape: Shape = Radii.sm,
) {
    val colors = Cars24.colors.shimmer
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerProgress",
    )

    val sweep = 600f
    val brush = Brush.linearGradient(
        colors = colors,
        start = Offset(progress * sweep - sweep, 0f),
        end = Offset(progress * sweep, 0f),
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush),
    )
}

@Preview(showBackground = true, widthDp = 340)
@Composable
private fun ShimmerBlockPreview() {
    Cars24Theme {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            ShimmerBlock(Modifier.fillMaxWidth().height(48.dp), Radii.md)
            ShimmerBlock(Modifier.width(180.dp).height(20.dp), Radii.sm)
        }
    }
}
