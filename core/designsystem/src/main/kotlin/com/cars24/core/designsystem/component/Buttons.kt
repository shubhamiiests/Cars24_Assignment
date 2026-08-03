package com.cars24.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.cars24.core.designsystem.theme.Cars24
import com.cars24.core.designsystem.theme.Radii
import com.cars24.core.designsystem.theme.Spacing

enum class Cars24ButtonStyle { Primary, Accent, Outline }

@Composable
fun Cars24Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: Cars24ButtonStyle = Cars24ButtonStyle.Primary,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val contentPadding = PaddingValues(horizontal = Spacing.xl, vertical = Spacing.md)

    if (style == Cars24ButtonStyle.Outline) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = Radii.md,
            border = BorderStroke(1.dp, Cars24.colors.divider),
            contentPadding = contentPadding,
        ) {
            ButtonBody(text, leadingIcon)
        }
        return
    }

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = Radii.md,
        colors = if (style == Cars24ButtonStyle.Accent) {
            ButtonDefaults.buttonColors(
                containerColor = Cars24.colors.accent,
                contentColor = Color.White,
            )
        } else {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
        contentPadding = contentPadding,
    ) {
        ButtonBody(text, leadingIcon)
    }
}

@Composable
private fun ButtonBody(text: String, leadingIcon: ImageVector?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Spacing.sm))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
