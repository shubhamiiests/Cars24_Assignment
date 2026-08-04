package com.cars24.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cars24.core.designsystem.theme.Cars24
import com.cars24.core.designsystem.theme.Radii
import com.cars24.core.designsystem.theme.Spacing
import androidx.compose.ui.tooling.preview.Preview
import com.cars24.core.designsystem.theme.Cars24Theme
import androidx.compose.foundation.layout.Column

@Composable
fun Cars24Chip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supporting: String? = null,
) {
    val colors = Cars24.colors
    val container by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else colors.cardSurface,
        label = "chipContainer",
    )
    val content by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else colors.textSecondary,
        label = "chipContent",
    )

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = Radii.pill,
        color = container,
        contentColor = content,
        border = BorderStroke(1.dp, if (selected) container else colors.divider),
    ) {
        Row(
            modifier = Modifier.padding(PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            )
            if (supporting != null) {
                Spacer(Modifier.width(Spacing.xs))
                Text(supporting, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun Cars24Tag(
    text: String,
    modifier: Modifier = Modifier,
    emphasised: Boolean = false,
) {
    val colors = Cars24.colors
    Surface(
        modifier = modifier,
        shape = Radii.sm,
        color = if (emphasised) colors.successContainer else colors.pageBackground,
        contentColor = if (emphasised) colors.success else colors.textSecondary,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
        )
    }
}

@Preview(showBackground = true, widthDp = 340)
@Preview(showBackground = true, widthDp = 340, uiMode = 0x20)
@Composable
private fun Cars24ChipPreview() {
    Cars24Theme {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Cars24Chip("All", selected = true, onClick = {}, supporting = "412")
                Cars24Chip("Petrol", selected = false, onClick = {}, supporting = "268")
                Cars24Chip("Diesel", selected = false, onClick = {}, supporting = "91")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Cars24Tag("Rs 11,400/mo")
                Cars24Tag("Cars24 Assured", emphasised = true)
            }
        }
    }
}
