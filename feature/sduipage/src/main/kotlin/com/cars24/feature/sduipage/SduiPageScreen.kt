package com.cars24.feature.sduipage

import androidx.compose.ui.res.stringResource
import com.cars24.data.page.PageFailure
import com.cars24.data.page.StaleReason
import com.cars24.feature.sduipage.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cars24.core.designsystem.component.ErrorState
import com.cars24.core.designsystem.component.OfflineState
import com.cars24.core.designsystem.component.PageSkeleton
import com.cars24.core.designsystem.component.StaleBanner
import com.cars24.core.designsystem.theme.Cars24
import com.cars24.core.designsystem.theme.Cars24Theme
import com.cars24.sdui.components.Cars24Components
import com.cars24.sdui.runtime.render.LocalSduiPageState
import com.cars24.sdui.runtime.render.SduiChildren
import com.cars24.sdui.runtime.render.SduiPageHost
import com.cars24.sdui.runtime.render.SduiScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.ui.tooling.preview.Preview
import com.cars24.data.page.PageOrigin
import com.cars24.sdui.schema.SduiJson
import com.cars24.sdui.schema.SduiPage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SduiPageScreen(
    state: PageUiState,
    onIntent: (PageIntent) -> Unit,
    showBackButton: Boolean = false,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val registry = remember { Cars24Components.registry() }
    val listState = rememberLazyListState()

    val latestState = rememberUpdatedState(state.pageState)
    val latestOnIntent = rememberUpdatedState(onIntent)
    val scope = remember(registry) {
        SduiScope(
            registry = registry,
            pageSchemaVersion = 1,
            showUnknownPlaceholders = true,
            stateProvider = { latestState.value },
            onCommand = { latestOnIntent.value(PageIntent.Command(it)) },
            onUnsupportedType = { latestOnIntent.value(PageIntent.UnsupportedComponent(it)) },
        )
    }



    Box(modifier = modifier.fillMaxSize()) {
        when (state.phase) {
            PagePhase.Loading -> PageSkeleton(Modifier.statusBarsPadding())

            PagePhase.Offline -> Column(Modifier.statusBarsPadding()) {
                if (showBackButton) PageTopBar(title = null, onBack = onBack)
                OfflineState(onRetry = { onIntent(PageIntent.Retry) })
            }

            PagePhase.Failed -> Column(Modifier.statusBarsPadding()) {
                if (showBackButton) PageTopBar(title = null, onBack = onBack)
                ErrorState(
                    onRetry = { onIntent(PageIntent.Retry) },
                    message = state.failure.toMessage(),
                )
            }

            PagePhase.Content -> {
                val page = state.page ?: return@Box
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { onIntent(PageIntent.Refresh) },
                ) {
                SduiPageHost(
                    page = page,
                    scope = scope,
                    pageState = state.pageState,
                    listState = listState,
                    contentPadding = PaddingValues(bottom = 24.dp),
                    header = {
                        Column(Modifier.statusBarsPadding()) {
                            if (showBackButton) {
                                PageTopBar(title = page.title, onBack = onBack)
                            }
                            val staleReason = state.staleReason
                            if (staleReason != null) {
                                StaleBanner(
                                    text = staleReason.toMessage(),
                                    onRetry = { onIntent(PageIntent.Retry) },
                                )
                            }
                        }
                    },
                )
                }
            }
        }
    }

    val sheet = state.openSheet
    if (sheet != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { onIntent(PageIntent.DismissSheet) },
            sheetState = sheetState,
            containerColor = Cars24.colors.cardSurface,
        ) {
            Column(Modifier.navigationBarsPadding()) {
                CompositionLocalProvider(LocalSduiPageState provides state.pageState) {
                    SduiChildren(sheet.content, scope)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun StaleReason.toMessage(): String = stringResource(
    when (this) {
        StaleReason.NoConnection -> R.string.page_stale_no_connection
        StaleReason.ServerPayloadUnusable -> R.string.page_stale_server_unusable
    },
)

@Composable
private fun PageFailure?.toMessage(): String = stringResource(
    when (this) {
        PageFailure.PushedPayloadUnparseable -> R.string.page_failed_pushed_payload
        else -> R.string.page_failed_server_payload
    },
)

@Composable
private fun PageTopBar(title: String?, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Cars24.colors.cardSurface)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = stringResource(R.string.page_cd_back),
                tint = Cars24.colors.textPrimary,
            )
        }
        Text(
            text = title.orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            color = Cars24.colors.textPrimary,
        )
    }
}


@Preview(showBackground = true, device = "spec:width=411dp,height=1200dp")
@Preview(showBackground = true, device = "spec:width=411dp,height=1200dp", uiMode = 0x20)
@Composable
private fun SduiPageContentPreview() {
    Cars24Theme { SduiPageScreen(state = previewPageState(), onIntent = {}) }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=1000dp")
@Composable
private fun SduiPageDieselTabPreview() {
    Cars24Theme { SduiPageScreen(state = previewPageState(fuel = "diesel"), onIntent = {}) }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=1000dp")
@Composable
private fun SduiPagePushedPreview() {
    Cars24Theme {
        SduiPageScreen(
            state = previewPageState(),
            onIntent = {},
            showBackButton = true,
            onBack = {},
        )
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=1000dp")
@Composable
private fun SduiPageStalePreview() {
    Cars24Theme {
        SduiPageScreen(
            state = previewPageState().copy(
                staleReason = StaleReason.NoConnection,
                origin = PageOrigin.Cache,
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun SduiPageLoadingPreview() {
    Cars24Theme { SduiPageScreen(state = PageUiState(phase = PagePhase.Loading), onIntent = {}) }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun SduiPageOfflinePreview() {
    Cars24Theme { SduiPageScreen(state = PageUiState(phase = PagePhase.Offline), onIntent = {}) }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun SduiPageFailedPreview() {
    Cars24Theme {
        SduiPageScreen(
            state = PageUiState(
                phase = PagePhase.Failed,
                failure = PageFailure.ServerPayloadUnparseable,
            ),
            onIntent = {},
        )
    }
}

@Composable
private fun previewPageState(fuel: String = "all"): PageUiState {
    val page = remember { SduiJson.format.decodeFromString<SduiPage>(PREVIEW_PAYLOAD) }
    return PageUiState(
        phase = PagePhase.Content,
        page = page,
        pageState = page.initialState + mapOf("fuel" to fuel),
        origin = PageOrigin.Network,
        parseMillis = 16,
        payloadBytes = PREVIEW_PAYLOAD.length,
    )
}

private const val PREVIEW_PAYLOAD = """
{
  "pageId": "home_preview",
  "schemaVersion": 2,
  "title": "Cars24",
  "background": "page",
  "initialState": {
    "city": "Gurgaon", "fuel": "all", "tenure": "48",
    "emi_monthly": "Rs 14,820", "emi_total": "Rs 7,11,360", "emi_tenure_label": "48 mo"
  },
  "sections": [
    { "id": "hero", "type": "search_header",
      "props": { "city": "{{state.city|Gurgaon}}", "greeting": "Find your next car",
                 "searchHint": "Search Swift, Baleno, i20, Nexon..." } },
    { "id": "g1", "type": "spacer", "props": { "size": 16 } },
    { "id": "quick", "type": "quick_actions",
      "props": { "actions": [
        { "label": "Buy car", "icon": "buy", "caption": "12,400 cars" },
        { "label": "Sell car", "icon": "sell", "caption": "Best price" },
        { "label": "Car loan", "icon": "loan", "caption": "From 9.7%" },
        { "label": "Insurance", "icon": "insurance", "caption": "Renew fast" } ] } },
    { "id": "g2", "type": "spacer", "props": { "size": 20 } },
    { "id": "banners", "type": "banner_carousel",
      "props": { "height": 150, "slides": [
        { "title": "Zero down payment", "subtitle": "On 2,000+ assured cars this month",
          "ctaLabel": "Check eligibility", "gradient": ["#1B2065", "#5865C4"] },
        { "title": "Sell in a single visit", "subtitle": "Instant payment, free RC transfer",
          "ctaLabel": "Get a quote", "gradient": ["#0B8A6B", "#3FCFA8"] } ] } },
    { "id": "g3", "type": "spacer", "props": { "size": 24 } },
    { "id": "budget_header", "type": "section_header",
      "props": { "title": "Cars in your budget", "subtitle": "Under 8 lakh, ready to drive",
                 "actionLabel": "View all" } },
    { "id": "g4", "type": "spacer", "props": { "size": 12 } },
    { "id": "fuel_tabs", "type": "chip_group",
      "props": { "stateKey": "fuel", "options": [
        { "label": "All", "value": "all", "supporting": "412" },
        { "label": "Petrol", "value": "petrol", "supporting": "268" },
        { "label": "Diesel", "value": "diesel", "supporting": "91" },
        { "label": "CNG", "value": "cng", "supporting": "53" } ] } },
    { "id": "g5", "type": "spacer", "props": { "size": 12 } },
    { "id": "rail_all", "type": "carousel", "props": { "itemSpacing": 12 },
      "visibleWhen": { "key": "fuel", "equals": "all" },
      "children": [
        { "id": "c_swift", "type": "car_card",
          "props": { "name": "Maruti Swift VXi", "price": "Rs 5.24 L", "emi": "Rs 11,400/mo",
                     "specs": ["2019", "42,150 km", "Petrol"], "savings": "Save 38k",
                     "assured": true, "wishKey": "wish_swift" } },
        { "id": "c_i20", "type": "car_card",
          "props": { "name": "Hyundai i20 Sportz", "price": "Rs 6.85 L", "emi": "Rs 14,820/mo",
                     "specs": ["2020", "31,900 km", "Petrol"], "badge": "Popular",
                     "assured": true, "wishKey": "wish_i20" } } ] },
    { "id": "rail_diesel", "type": "carousel", "props": { "itemSpacing": 12 },
      "visibleWhen": { "key": "fuel", "equals": "diesel" },
      "children": [
        { "id": "c_nexon", "type": "car_card",
          "props": { "name": "Tata Nexon XZ+", "price": "Rs 7.95 L", "emi": "Rs 17,190/mo",
                     "specs": ["2019", "55,600 km", "Diesel"], "wishKey": "wish_nexon" } },
        { "id": "c_eco", "type": "car_card",
          "props": { "name": "Ford EcoSport Titanium", "price": "Rs 6.75 L", "emi": "Rs 14,600/mo",
                     "specs": ["2018", "68,200 km", "Diesel"], "savings": "Save 52k",
                     "wishKey": "wish_eco" } } ] },
    { "id": "rail_petrol", "type": "carousel", "props": { "itemSpacing": 12 },
      "visibleWhen": { "key": "fuel", "equals": "petrol" },
      "children": [
        { "id": "c_baleno", "type": "car_card",
          "props": { "name": "Maruti Baleno Zeta", "price": "Rs 6.40 L", "emi": "Rs 13,850/mo",
                     "specs": ["2020", "36,800 km", "Petrol"], "assured": true,
                     "wishKey": "wish_baleno" } } ] },
    { "id": "rail_cng", "type": "carousel", "props": { "itemSpacing": 12 },
      "visibleWhen": { "key": "fuel", "equals": "cng" },
      "children": [
        { "id": "c_wagonr", "type": "car_card",
          "props": { "name": "Maruti WagonR LXi", "price": "Rs 4.60 L", "emi": "Rs 9,950/mo",
                     "specs": ["2021", "28,400 km", "CNG"], "badge": "Low km",
                     "wishKey": "wish_wagonr" } } ] },
    { "id": "g6", "type": "spacer", "props": { "size": 28 } },
    { "id": "emi_header", "type": "section_header",
      "props": { "title": "Plan your EMI", "subtitle": "On a Rs 6.85 L car with Rs 1.2 L down payment" } },
    { "id": "g7", "type": "spacer", "props": { "size": 12 } },
    { "id": "tenure", "type": "chip_group",
      "props": { "stateKey": "tenure", "scrollable": false, "options": [
        { "label": "36 mo", "value": "36" }, { "label": "48 mo", "value": "48" },
        { "label": "60 mo", "value": "60" }, { "label": "72 mo", "value": "72" } ] },
      "style": { "padding": { "horizontal": 16 } } },
    { "id": "g8", "type": "spacer", "props": { "size": 16 } },
    { "id": "emi_card", "type": "emi_summary",
      "props": { "heading": "Your monthly EMI", "monthly": "{{state.emi_monthly}}",
                 "monthlyCaption": "for {{state.emi_tenure_label}}",
                 "rows": [ { "label": "Total payable", "value": "{{state.emi_total}}" },
                           { "label": "Interest rate", "value": "9.7% p.a." } ],
                 "ctaLabel": "See full breakdown" } },
    { "id": "g9", "type": "spacer", "props": { "size": 28 } },
    { "id": "trust", "type": "value_props",
      "props": { "heading": "Every Cars24 car comes with", "items": [
        { "title": "140-point", "caption": "inspection", "icon": "inspection" },
        { "title": "7-day", "caption": "money back", "icon": "return" },
        { "title": "Free RC", "caption": "transfer", "icon": "paperwork" },
        { "title": "1-year", "caption": "warranty", "icon": "warranty" } ] } },
    { "id": "g10", "type": "spacer", "props": { "size": 24 } },
    { "id": "unknown_demo", "type": "ar_showroom_360", "props": { "carId": "creta_sx" } },
    { "id": "too_new_demo", "type": "loyalty_tier_card", "minSchemaVersion": 99,
      "props": { "tier": "gold" },
      "fallback": { "id": "too_new_fb", "type": "value_props",
        "props": { "heading": "Cars24 rewards",
                   "items": [ { "title": "Update", "caption": "to see rewards", "icon": "warranty" } ] } } },
    { "id": "g11", "type": "spacer", "props": { "size": 24 } },
    { "id": "faq", "type": "faq_item",
      "props": { "question": "How does the 7-day money back work?",
                 "answer": "Drive the car for up to 7 days or 350 km, then return it at any hub for a full refund.",
                 "startExpanded": true } },
    { "id": "g12", "type": "spacer", "props": { "size": 20 } },
    { "id": "footer", "type": "column", "props": { "spacing": 12, "align": "center" },
      "style": { "margin": { "horizontal": 16 }, "padding": { "all": 24 },
                 "gradient": ["#11144B", "#2E3A8C"], "cornerRadius": 20 },
      "children": [
        { "id": "f_title", "type": "text",
          "props": { "value": "Sell your car in a single visit", "style": "title_large",
                     "color": "white", "align": "center" } },
        { "id": "f_button", "type": "button",
          "props": { "label": "Get a free quote", "variant": "accent" } } ] },
    { "id": "g13", "type": "spacer", "props": { "size": 32 } }
  ]
}
"""
