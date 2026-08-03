package com.cars24.sdui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cars24.feature.sduipage.SduiPageRoute
import com.cars24.sdui.R
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Composable
fun SduiNavHost(
    knownPages: Set<String>,
    snackbarHostState: SnackbarHostState,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val coroutineScope = rememberCoroutineScope()
    val unsupportedActionMessage = stringResource(R.string.page_unsupported_action)
    val noPayloadTemplate = stringResource(R.string.app_no_payload_for_route)
    val noPayloadMessage: (String) -> String = { route -> noPayloadTemplate.format(route) }

    NavHost(
        navController = navController,
        startDestination = pageRoute(START_PAGE),
        modifier = modifier,
    ) {
        composable(
            route = "$PAGE_ROUTE_PREFIX/{$ARG_PAGE_ID}?$ARG_PARAMS={$ARG_PARAMS}",
            arguments = listOf(
                navArgument(ARG_PAGE_ID) { type = NavType.StringType },
                navArgument(ARG_PARAMS) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { entry ->
            val pageId = entry.arguments?.getString(ARG_PAGE_ID) ?: START_PAGE
            val routeParams = decodeParams(entry.arguments?.getString(ARG_PARAMS))

            SduiPageRoute(
                pageId = pageId,
                routeParams = routeParams,
                showBackButton = pageId != START_PAGE,
                onBack = { navController.popBackStack() },
                onNavigate = { route, params ->
                    if (route in knownPages) {
                        navController.navigate(pageRoute(route, params))
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(noPayloadMessage(route))
                        }
                    }
                },
                onOpenUrl = onOpenUrl,
                onUnsupportedAction = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(unsupportedActionMessage)
                    }
                },
            )
        }
    }
}

@Composable
fun StaticBaselineHost(content: @Composable (Modifier) -> Unit) {
    Box(Modifier.fillMaxSize()) { content(Modifier.padding()) }
}

private const val PAGE_ROUTE_PREFIX = "page"
private const val ARG_PAGE_ID = "pageId"
private const val ARG_PARAMS = "args"
private const val START_PAGE = "home"

private fun pageRoute(pageId: String, params: Map<String, String> = emptyMap()): String {
    if (params.isEmpty()) return "$PAGE_ROUTE_PREFIX/$pageId"
    val encoded = Uri.encode(Json.encodeToString(params))
    return "$PAGE_ROUTE_PREFIX/$pageId?$ARG_PARAMS=$encoded"
}

private fun decodeParams(raw: String?): Map<String, String> {
    if (raw.isNullOrEmpty()) return emptyMap()
    return runCatching { Json.decodeFromString<Map<String, String>>(raw) }.getOrDefault(emptyMap())
}

@Composable
fun rememberKnownPages(pageIds: Set<String>): Set<String> = remember(pageIds) { pageIds }
