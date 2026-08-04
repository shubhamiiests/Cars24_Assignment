package com.cars24.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.cars24.core.designsystem.theme.Cars24
import com.cars24.core.designsystem.theme.CarPhotoPlaceholderPalettes
import com.cars24.core.designsystem.theme.Elevations
import com.cars24.core.designsystem.theme.Radii
import androidx.compose.ui.tooling.preview.Preview
import com.cars24.core.designsystem.theme.Cars24Theme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import com.cars24.core.designsystem.theme.Spacing

@Composable
fun Cars24Card(
    modifier: Modifier = Modifier,
    shape: Shape = Radii.lg,
    bordered: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = Cars24.colors
    Surface(
        modifier = modifier,
        shape = shape,
        color = colors.cardSurface,
        shadowElevation = Elevations.card,
    ) {
        Box(
            modifier = if (bordered) {
                Modifier.border(1.dp, colors.divider, shape)
            } else {
                Modifier
            },
            content = content,
        )
    }
}

@Composable
fun ImagePlaceholder(
    seed: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(gradientFor(seed)),
    )
}



fun gradientFor(seed: String): Brush = Brush.linearGradient(paletteFor(seed))

private fun paletteFor(seed: String): List<Color> {
    var hash = seed.hashCode()
    hash = hash xor (hash ushr 16)
    hash *= 0x7feb352d
    hash = hash xor (hash ushr 15)
    return CarPhotoPlaceholderPalettes[(hash and Int.MAX_VALUE) % CarPhotoPlaceholderPalettes.size]
}

@Preview(showBackground = true, widthDp = 340)
@Composable
private fun Cars24CardPreview() {
    Cars24Theme {
        Cars24Card(modifier = Modifier.fillMaxWidth().padding(Spacing.lg)) {
            Text("Every section sits on one of these", modifier = Modifier.padding(Spacing.lg))
        }
    }
}
