package com.cars24.sdui.runtime.render

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cars24.core.designsystem.theme.Cars24
import com.cars24.core.designsystem.theme.Cars24Colors
import com.cars24.sdui.schema.SduiEdges
import com.cars24.sdui.schema.SduiStyle

@Composable
fun SduiStyle?.toModifier(): Modifier {
    if (this == null) return Modifier

    val colors = Cars24.colors
    val shape = cornerRadius?.let { RoundedCornerShape(it.dp) }
    var modifier: Modifier = Modifier

    margin?.let { modifier = modifier.padding(it.toPadding()) }

    when (val requestedWidth = width) {
        null -> Unit
        SIZE_FILL -> modifier = modifier.fillMaxWidth()
        SIZE_WRAP -> modifier = modifier.wrapContentWidth()
        else -> requestedWidth.toIntOrNull()?.let { modifier = modifier.width(it.dp) }
    }

    when (val requestedHeight = height) {
        null -> Unit
        SIZE_FILL -> modifier = modifier.fillMaxHeight()
        SIZE_WRAP -> modifier = modifier.wrapContentHeight()
        else -> requestedHeight.toIntOrNull()?.let { modifier = modifier.height(it.dp) }
    }

    aspectRatio?.takeIf { it > 0f }?.let { modifier = modifier.aspectRatio(it) }

    val shadowDepth = elevation ?: 0
    if (shadowDepth > 0 && shape != null) {
        modifier = modifier.shadow(shadowDepth.dp, shape)
    }

    if (shape != null) modifier = modifier.clip(shape)

    val gradientBrush = gradient?.toBrush(colors)
    val solid = background?.let { resolveColor(it, colors) }
    when {
        gradientBrush != null -> modifier = modifier.background(gradientBrush)
        solid != null -> modifier = modifier.background(solid)
    }

    val stroke = borderWidth ?: 0
    if (stroke > 0) {
        val borderTone = borderColor?.let { resolveColor(it, colors) } ?: colors.divider
        modifier = if (shape != null) {
            modifier.border(stroke.dp, borderTone, shape)
        } else {
            modifier.border(stroke.dp, borderTone)
        }
    }

    padding?.let { modifier = modifier.padding(it.toPadding()) }
    alpha?.let { modifier = modifier.alpha(it) }

    return modifier
}

private const val SIZE_FILL = "fill"
private const val SIZE_WRAP = "wrap"

private fun SduiEdges.toPadding() = androidx.compose.foundation.layout.PaddingValues(
    start = resolvedStart.dp,
    top = resolvedTop.dp,
    end = resolvedEnd.dp,
    bottom = resolvedBottom.dp,
)

private fun List<String>.toBrush(colors: Cars24Colors): Brush? {
    val stops = mapNotNull { resolveColor(it, colors) }
    return when {
        stops.size >= 2 -> Brush.verticalGradient(stops)
        else -> null
    }
}

fun resolveColor(value: String, colors: Cars24Colors): Color? = when (value.lowercase()) {
    "transparent" -> Color.Transparent
    "surface", "card" -> colors.cardSurface
    "background", "page" -> colors.pageBackground
    "primary", "brand" -> colors.brandGradient.first()
    "brand_dark" -> colors.brandGradient.last()
    "accent" -> colors.accent
    "accent_container" -> colors.accentContainer
    "success" -> colors.success
    "success_container" -> colors.successContainer
    "danger" -> colors.danger
    "danger_container" -> colors.dangerContainer
    "divider" -> colors.divider
    "text_primary" -> colors.textPrimary
    "text_secondary" -> colors.textSecondary
    "text_tertiary" -> colors.textTertiary
    "price" -> colors.price
    "white" -> Color.White
    else -> value.toHexColorOrNull()
}

private fun String.toHexColorOrNull(): Color? {
    if (!startsWith("#")) return null
    val hex = drop(1)
    val value = hex.toLongOrNull(radix = 16) ?: return null
    return when (hex.length) {
        6 -> Color(value or 0xFF000000L)
        8 -> Color(value)
        else -> null
    }
}
