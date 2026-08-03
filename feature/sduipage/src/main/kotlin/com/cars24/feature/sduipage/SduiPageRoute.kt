package com.cars24.feature.sduipage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SduiPageRoute(
    pageId: String,
    routeParams: Map<String, String> = emptyMap(),
    showBackButton: Boolean,
    onBack: () -> Unit,
    onNavigate: (route: String, params: Map<String, String>) -> Unit,
    onOpenUrl: (String) -> Unit,
    onUnsupportedAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SduiPageViewModel =
        koinViewModel(key = pageId) { parametersOf(pageId, routeParams) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    val currentOnNavigate by rememberUpdatedState(onNavigate)
    val currentOnOpenUrl by rememberUpdatedState(onOpenUrl)
    val currentOnUnsupported by rememberUpdatedState(onUnsupportedAction)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PageEffect.Navigate -> currentOnNavigate(effect.route, effect.params)
                is PageEffect.OpenUrl -> currentOnOpenUrl(effect.url)
                PageEffect.UnsupportedAction -> currentOnUnsupported()
            }
        }
    }

    SduiPageScreen(
        state = state,
        onIntent = viewModel::dispatch,
        showBackButton = showBackButton,
        onBack = onBack,
        modifier = modifier,
    )
}
