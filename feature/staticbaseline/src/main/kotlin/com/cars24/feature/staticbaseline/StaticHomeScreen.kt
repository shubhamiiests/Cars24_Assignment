package com.cars24.feature.staticbaseline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cars24.core.common.perf.StartupTrace
import com.cars24.core.designsystem.component.Cars24Button
import com.cars24.core.designsystem.component.Cars24ButtonStyle
import com.cars24.core.designsystem.component.Cars24Card
import com.cars24.core.designsystem.component.Cars24Tag
import com.cars24.core.designsystem.component.gradientFor
import com.cars24.core.designsystem.theme.Cars24
import com.cars24.core.designsystem.theme.PriceTextStyle
import com.cars24.core.designsystem.theme.Radii
import com.cars24.core.designsystem.theme.Spacing
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.first

@Composable
fun StaticHomeScreen(modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    var fuel by rememberSaveable { mutableStateOf("all") }
    var tenureIndex by rememberSaveable { mutableStateOf(1) }
    var sheetOpen by rememberSaveable { mutableStateOf(false) }

    val tenure = StaticHomeData.tenures[tenureIndex]
    val cars = StaticHomeData.carsByFuel.getValue(fuel)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Cars24.colors.pageBackground),
        state = listState,
        contentPadding = PaddingValues(bottom = Spacing.xxl),
    ) {
        item(key = "header") { StaticHeader(Modifier.statusBarsPadding()) }
        item(key = "gap_1") { Spacer(Modifier.height(Spacing.lg)) }
        item(key = "quick") { StaticQuickActions() }
        item(key = "gap_2") { Spacer(Modifier.height(Spacing.xl)) }
        item(key = "banners") { StaticBanners() }
        item(key = "gap_3") { Spacer(Modifier.height(Spacing.xxl)) }

        item(key = "budget_header") {
            StaticSectionHeader("Cars in your budget", "Under 8 lakh, ready to drive", "View all")
        }
        item(key = "gap_4") { Spacer(Modifier.height(Spacing.md)) }
        item(key = "fuel_tabs") {
            StaticChipRow(
                options = StaticHomeData.fuelTabs.map { Triple(it.first, it.second, it.third) },
                selected = fuel,
                onSelect = { fuel = it },
            )
        }
        item(key = "gap_5") { Spacer(Modifier.height(Spacing.md)) }
        item(key = "rail") {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                contentPadding = PaddingValues(horizontal = Spacing.lg),
            ) {
                items(count = cars.size, key = { cars[it].name }) { index ->
                    StaticCarCard(cars[index])
                }
            }
        }
        item(key = "gap_6") { Spacer(Modifier.height(28.dp)) }

        item(key = "emi_header") {
            StaticSectionHeader("Plan your EMI", "On a Rs 6.85 L car with Rs 1.2 L down payment", null)
        }
        item(key = "gap_7") { Spacer(Modifier.height(Spacing.md)) }
        item(key = "tenure") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                StaticHomeData.tenures.forEachIndexed { index, option ->
                    StaticChip(
                        label = option.label,
                        selected = index == tenureIndex,
                        onClick = { tenureIndex = index },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        item(key = "gap_8") { Spacer(Modifier.height(Spacing.lg)) }
        item(key = "emi_card") {
            StaticEmiCard(tenure = tenure, onCta = { sheetOpen = true })
        }
        item(key = "gap_9") { Spacer(Modifier.height(28.dp)) }

        item(key = "trust") { StaticValueProps() }
        item(key = "gap_10") { Spacer(Modifier.height(28.dp)) }

        item(key = "assured_header") {
            StaticSectionHeader("Assured cars near you", "Inspected, serviced and ready for delivery", "See all")
        }
        item(key = "gap_11") { Spacer(Modifier.height(Spacing.md)) }
        item(key = "assured_grid") {
            Column(
                modifier = Modifier.padding(horizontal = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                StaticHomeData.assured.chunked(2).forEach { rowCars ->
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        rowCars.forEach { car ->
                            Column(Modifier.weight(1f)) { StaticCarCard(car, fillWidth = true) }
                        }
                        repeat(2 - rowCars.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
        item(key = "gap_12") { Spacer(Modifier.height(28.dp)) }

        item(key = "faq_header") { StaticSectionHeader("Questions, answered", null, null) }
        item(key = "gap_13") { Spacer(Modifier.height(Spacing.md)) }
        StaticHomeData.faqs.forEachIndexed { index, faq ->
            item(key = "faq_$index") {
                StaticFaqRow(faq, startExpanded = index == 0)
                Spacer(Modifier.height(Spacing.sm))
            }
        }
        item(key = "gap_14") { Spacer(Modifier.height(Spacing.xl)) }
        item(key = "footer") { StaticFooterCta() }
        item(key = "gap_15") { Spacer(Modifier.height(Spacing.xxxl)) }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.isNotEmpty() }.first { it }
        StartupTrace.mark(StartupTrace.MARK_FIRST_SECTION_DRAWN)
        StartupTrace.mark(StartupTrace.MARK_INTERACTIVE)
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
            .first { it >= listState.layoutInfo.totalItemsCount - 1 }
        StartupTrace.mark(StartupTrace.MARK_FULL_PAGE)
    }

    if (sheetOpen) {
        StaticEmiSheet(tenure = tenure, onDismiss = { sheetOpen = false })
    }
}

@Composable
private fun StaticHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(Cars24.colors.brandGradient))
            .padding(start = Spacing.lg, end = Spacing.lg, top = Spacing.lg, bottom = Spacing.xl),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocationOn, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Spacing.xs))
            Column {
                Text(
                    "Delivering to",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        StaticHomeData.CITY,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                    )
                    Icon(
                        Icons.Filled.KeyboardArrowRight,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Text("CARS24", style = MaterialTheme.typography.titleLarge, color = Color.White)
        }

        Spacer(Modifier.height(Spacing.lg))
        Text(
            StaticHomeData.GREETING,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
        )
        Spacer(Modifier.height(Spacing.lg))

        Surface(modifier = Modifier.fillMaxWidth(), shape = Radii.md, color = Color.White) {
            Row(
                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Search,
                    null,
                    tint = Cars24.colors.textTertiary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    StaticHomeData.SEARCH_HINT,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Cars24.colors.textTertiary,
                )
            }
        }
    }
}

@Composable
private fun StaticQuickActions() {
    val icons = listOf(
        Icons.Filled.ShoppingCart,
        Icons.Filled.Star,
        Icons.Filled.DateRange,
        Icons.Filled.Lock,
    )
    Cars24Card(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.lg),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StaticHomeData.quickActions.forEachIndexed { index, (label, caption) ->
                Column(
                    modifier = Modifier.weight(1f).padding(vertical = Spacing.xs),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(Radii.md)
                            .background(Cars24.colors.accentContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            icons[index],
                            null,
                            tint = Cars24.colors.accent,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        label,
                        style = MaterialTheme.typography.labelMedium,
                        color = Cars24.colors.textPrimary,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        caption,
                        style = MaterialTheme.typography.labelSmall,
                        color = Cars24.colors.textTertiary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun StaticBanners() {
    val listState = rememberLazyListState()
    Column {
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            contentPadding = PaddingValues(horizontal = Spacing.lg),
        ) {
            items(count = StaticHomeData.banners.size, key = { StaticHomeData.banners[it].title }) { index ->
                val banner = StaticHomeData.banners[index]
                Column(
                    modifier = Modifier
                        .width(300.dp)
                        .height(150.dp)
                        .clip(Radii.lg)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(banner.from), Color(banner.to)),
                            ),
                        )
                        .padding(Spacing.xl),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(banner.title, style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        banner.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                    Spacer(Modifier.height(Spacing.md))
                    Text(
                        banner.cta,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        modifier = Modifier
                            .clip(Radii.pill)
                            .background(Color.White.copy(alpha = 0.22f))
                            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                    )
                }
            }
        }
        Spacer(Modifier.height(Spacing.sm))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            repeat(StaticHomeData.banners.size) { index ->
                val active = index == listState.firstVisibleItemIndex
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(width = if (active) 18.dp else 6.dp, height = 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (active) MaterialTheme.colorScheme.primary else Cars24.colors.divider,
                        ),
                )
            }
        }
    }
}

@Composable
private fun StaticSectionHeader(title: String, subtitle: String?, action: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = Cars24.colors.textPrimary)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Cars24.colors.textSecondary,
                )
            }
        }
        if (action != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    action,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Icon(
                    Icons.Filled.KeyboardArrowRight,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun StaticChipRow(
    options: List<Triple<String, String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        contentPadding = PaddingValues(horizontal = Spacing.lg),
    ) {
        items(count = options.size, key = { options[it].second }) { index ->
            val (label, value, supporting) = options[index]
            StaticChip(
                label = label,
                selected = value == selected,
                onClick = { onSelect(value) },
                supporting = supporting,
            )
        }
    }
}

@Composable
private fun StaticChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supporting: String? = null,
) {
    val container by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else Cars24.colors.cardSurface,
        label = "staticChipContainer",
    )
    val content by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onPrimary else Cars24.colors.textSecondary,
        label = "staticChipContent",
    )

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = Radii.pill,
        color = container,
        contentColor = content,
        border = BorderStroke(1.dp, if (selected) container else Cars24.colors.divider),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            if (supporting != null) {
                Spacer(Modifier.width(Spacing.xs))
                Text(supporting, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun StaticCarCard(car: StaticCar, fillWidth: Boolean = false) {
    Cars24Card(
        modifier = if (fillWidth) Modifier.fillMaxWidth() else Modifier.width(220.dp),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(gradientFor(car.name), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            ) {
                if (car.badge != null) {
                    Surface(
                        modifier = Modifier.padding(Spacing.sm),
                        shape = Radii.sm,
                        color = Color.White.copy(alpha = 0.92f),
                    ) {
                        Text(
                            car.badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = Cars24.colors.textPrimary,
                            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
                        )
                    }
                }
                Icon(
                    Icons.Filled.FavoriteBorder,
                    "Save",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Spacing.sm)
                        .size(20.dp),
                )
            }

            Column(Modifier.padding(Spacing.md)) {
                Text(
                    car.specs.joinToString(" • "),
                    style = MaterialTheme.typography.labelSmall,
                    color = Cars24.colors.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(Spacing.xxs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        car.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = Cars24.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (car.assured) {
                        Spacer(Modifier.width(Spacing.xs))
                        Icon(
                            Icons.Filled.CheckCircle,
                            "Cars24 assured",
                            tint = Cars24.colors.success,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.sm))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(car.price, style = PriceTextStyle, color = Cars24.colors.price)
                    if (car.savings != null) {
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            car.savings,
                            style = MaterialTheme.typography.labelSmall,
                            color = Cars24.colors.success,
                            modifier = Modifier.padding(bottom = 2.dp),
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.xs))
                Cars24Tag(text = car.emi)
            }
        }
    }
}

@Composable
private fun StaticEmiCard(tenure: StaticTenure, onCta: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
            .clip(Radii.lg)
            .background(Brush.horizontalGradient(Cars24.colors.brandGradient))
            .padding(Spacing.xl),
    ) {
        Text(
            "Your monthly EMI",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.75f),
        )
        Spacer(Modifier.height(Spacing.xs))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(tenure.monthly, style = MaterialTheme.typography.displaySmall, color = Color.White)
            Spacer(Modifier.width(Spacing.sm))
            Text(
                "for ${tenure.label}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.75f),
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        Spacer(Modifier.height(Spacing.lg))
        StaticEmiRow("Total payable", tenure.total)
        StaticEmiRow("Interest rate", "9.7% p.a.")
        StaticEmiRow("Down payment", "Rs 1,20,000")
        Spacer(Modifier.height(Spacing.lg))
        Cars24Button(
            text = "See full breakdown",
            onClick = onCta,
            modifier = Modifier.fillMaxWidth(),
            style = Cars24ButtonStyle.Accent,
        )
    }
}

@Composable
private fun StaticEmiRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xxs),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
        Text(value, style = MaterialTheme.typography.labelLarge, color = Color.White)
    }
}

@Composable
private fun StaticValueProps() {
    val icons = listOf(
        Icons.Filled.CheckCircle,
        Icons.Filled.Refresh,
        Icons.Filled.List,
        Icons.Filled.Build,
    )
    Cars24Card(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg)) {
        Column(Modifier.padding(Spacing.lg)) {
            Text(
                "Every Cars24 car comes with",
                style = MaterialTheme.typography.titleMedium,
                color = Cars24.colors.textPrimary,
            )
            Spacer(Modifier.height(Spacing.lg))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StaticHomeData.valueProps.forEachIndexed { index, prop ->
                    Column(
                        modifier = Modifier.weight(1f).padding(horizontal = Spacing.xs),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(Radii.pill)
                                .background(Cars24.colors.successContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                icons[index],
                                null,
                                tint = Cars24.colors.success,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(Modifier.height(Spacing.sm))
                        Text(
                            prop.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = Cars24.colors.textPrimary,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            prop.caption,
                            style = MaterialTheme.typography.labelSmall,
                            color = Cars24.colors.textTertiary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StaticFaqRow(faq: StaticFaq, startExpanded: Boolean) {
    var expanded by rememberSaveable(faq.question) { mutableStateOf(startExpanded) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "staticFaqChevron")

    Cars24Card(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg)) {
        Column(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(Spacing.lg),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    faq.question,
                    style = MaterialTheme.typography.titleSmall,
                    color = Cars24.colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    if (expanded) "Collapse" else "Expand",
                    tint = Cars24.colors.textSecondary,
                    modifier = Modifier.size(22.dp).rotate(rotation),
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        faq.answer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Cars24.colors.textSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun StaticFooterCta() {
    Column(
        modifier = Modifier
            .padding(horizontal = Spacing.lg)
            .clip(Radii.xl)
            .background(Brush.verticalGradient(listOf(Color(0xFF11144B), Color(0xFF2E3A8C))))
            .padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text(
            "Sell your car in a single visit",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Text(
            "Free evaluation, instant payment, and we take care of the paperwork.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Cars24Button(
            text = "Get a free quote",
            onClick = {},
            style = Cars24ButtonStyle.Accent,
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun StaticEmiSheet(tenure: StaticTenure, onDismiss: () -> Unit) {
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Cars24.colors.cardSurface,
    ) {
        Column(Modifier.padding(bottom = Spacing.xxl)) {
            Text(
                "EMI breakdown",
                style = MaterialTheme.typography.headlineSmall,
                color = Cars24.colors.textPrimary,
                modifier = Modifier.padding(horizontal = Spacing.xl),
            )
            Text(
                "For ${tenure.label} at 9.7% p.a.",
                style = MaterialTheme.typography.bodySmall,
                color = Cars24.colors.textSecondary,
                modifier = Modifier.padding(horizontal = Spacing.xl, vertical = Spacing.xs),
            )
            Spacer(Modifier.height(Spacing.xl))
            StaticEmiCard(tenure = tenure, onCta = onDismiss)
            Spacer(Modifier.height(Spacing.lg))
            Text(
                "Rates are indicative and depend on your credit profile. Final terms are shared by the lending partner before you sign.",
                style = MaterialTheme.typography.labelSmall,
                color = Cars24.colors.textTertiary,
                modifier = Modifier.padding(horizontal = Spacing.xl),
            )
        }
    }
}
