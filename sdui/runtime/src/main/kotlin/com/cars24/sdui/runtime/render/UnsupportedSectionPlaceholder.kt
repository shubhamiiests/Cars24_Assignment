package com.cars24.sdui.runtime.render

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cars24.core.designsystem.theme.Cars24
import com.cars24.core.designsystem.theme.Radii
import com.cars24.core.designsystem.theme.Spacing

@Composable
internal fun UnsupportedSectionPlaceholder(
    type: String,
    reason: DegradeReason,
    modifier: Modifier = Modifier,
) {
    val colors = Cars24.colors
    val headline = when (reason) {
        DegradeReason.UnknownType -> "Section not supported yet"
        DegradeReason.SchemaTooNew -> "Update the app to see this"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
            .border(1.dp, colors.divider, Radii.md)
            .background(colors.pageBackground, Radii.md)
            .padding(Spacing.lg),
    ) {
        Text(
            text = headline,
            style = MaterialTheme.typography.titleSmall,
            color = colors.textSecondary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.xxs))
        Text(
            text = "type: $type",
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}
