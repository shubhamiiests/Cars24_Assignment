package com.cars24.core.designsystem.component

import com.cars24.core.designsystem.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cars24.core.designsystem.theme.Cars24
import com.cars24.core.designsystem.theme.Radii
import com.cars24.core.designsystem.theme.Spacing

@Composable
fun OfflineState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.ds_offline_title),
    message: String = stringResource(R.string.ds_offline_message),
) {
    val colors = Cars24.colors
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.pageBackground)
            .padding(Spacing.xxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(colors.dangerContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                tint = colors.danger,
                modifier = Modifier.size(32.dp),
            )
        }
        Spacer(Modifier.height(Spacing.xl))
        Text(title, style = MaterialTheme.typography.headlineSmall, color = colors.textPrimary)
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.xxl))
        Cars24Button(
            text = stringResource(R.string.ds_action_try_again),
            onClick = onRetry,
            leadingIcon = Icons.Filled.Refresh,
        )
    }
}

@Composable
fun ErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    message: String = stringResource(R.string.ds_error_message_default),
) {
    OfflineState(
        onRetry = onRetry,
        modifier = modifier,
        title = stringResource(R.string.ds_error_title),
        message = message,
    )
}

@Composable
fun StaleBanner(
    text: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Cars24.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.accentContainer)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(Spacing.sm))
        Row(
            modifier = Modifier
                .clip(Radii.sm)
                .clickable(onClick = onRetry)
                .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(Spacing.xs))
            Text(
                text = stringResource(R.string.ds_action_retry_short),
                style = MaterialTheme.typography.labelMedium,
                color = colors.accent,
            )
        }
    }
}

@Composable
fun PageSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Cars24.colors.pageBackground)
            .padding(Spacing.lg),
    ) {
        ShimmerBlock(Modifier.fillMaxWidth().height(48.dp), Radii.md)
        Spacer(Modifier.height(Spacing.lg))
        Row {
            repeat(4) {
                ShimmerBlock(Modifier.width(76.dp).height(32.dp), Radii.pill)
                Spacer(Modifier.width(Spacing.sm))
            }
        }
        Spacer(Modifier.height(Spacing.lg))
        ShimmerBlock(Modifier.fillMaxWidth().height(140.dp), Radii.lg)
        Spacer(Modifier.height(Spacing.lg))
        ShimmerBlock(Modifier.width(160.dp).height(20.dp), Radii.sm)
        Spacer(Modifier.height(Spacing.md))
        Row {
            repeat(2) {
                ShimmerBlock(Modifier.width(200.dp).height(220.dp), Radii.lg)
                Spacer(Modifier.width(Spacing.md))
            }
        }
    }
}
