package com.cars24.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import androidx.compose.ui.tooling.preview.Preview
import com.cars24.core.designsystem.theme.Cars24Theme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import com.cars24.core.designsystem.theme.Radii
import com.cars24.core.designsystem.theme.Spacing

@Composable
fun NetworkImage(
    url: String?,
    seed: String,
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    contentDescription: String? = null,
) {
    val shaped = if (shape != null) modifier.clip(shape) else modifier

    Box(modifier = shaped.background(gradientFor(seed))) {
        if (!url.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .crossfade(CROSSFADE_MS)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

private const val CROSSFADE_MS = 220

@Preview(showBackground = true, widthDp = 340)
@Composable
private fun NetworkImagePreview() {
    Cars24Theme {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            listOf("Maruti Swift VXi", "Hyundai i20 Sportz", "Hyundai Creta SX", "Tata Nexon XZ+")
                .forEach { seed ->
                    NetworkImage(
                        url = null,
                        seed = seed,
                        shape = Radii.sm,
                        modifier = Modifier.width(68.dp).height(68.dp),
                    )
                }
        }
    }
}
