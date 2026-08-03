package com.cars24.sdui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.cars24.core.designsystem.theme.PriceTextStyle

@Composable
@ReadOnlyComposable
fun textStyleToken(token: String?): TextStyle {
    val type = MaterialTheme.typography
    return when (token) {
        "display_small" -> type.displaySmall
        "headline_medium" -> type.headlineMedium
        "headline_small" -> type.headlineSmall
        "title_large" -> type.titleLarge
        "title_medium" -> type.titleMedium
        "title_small" -> type.titleSmall
        "body_large" -> type.bodyLarge
        "body_small" -> type.bodySmall
        "label_large" -> type.labelLarge
        "label_medium" -> type.labelMedium
        "label_small" -> type.labelSmall
        "price" -> PriceTextStyle
        else -> type.bodyMedium
    }
}

fun fontWeightToken(token: String?): FontWeight? = when (token) {
    "normal" -> FontWeight.Normal
    "medium" -> FontWeight.Medium
    "semibold" -> FontWeight.SemiBold
    "bold" -> FontWeight.Bold
    else -> null
}

fun textAlignToken(token: String?): TextAlign? = when (token) {
    LayoutToken.START -> TextAlign.Start
    LayoutToken.CENTER -> TextAlign.Center
    LayoutToken.END -> TextAlign.End
    else -> null
}

fun horizontalAlignmentToken(token: String?): Alignment.Horizontal = when (token) {
    LayoutToken.CENTER -> Alignment.CenterHorizontally
    LayoutToken.END -> Alignment.End
    else -> Alignment.Start
}

fun verticalAlignmentToken(token: String?): Alignment.Vertical = when (token) {
    LayoutToken.CENTER -> Alignment.CenterVertically
    LayoutToken.BOTTOM -> Alignment.Bottom
    else -> Alignment.Top
}
