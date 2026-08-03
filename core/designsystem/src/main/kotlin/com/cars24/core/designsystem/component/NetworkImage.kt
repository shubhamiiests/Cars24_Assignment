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
