package com.cars24.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeRoute(
    onNavigate: (route: String, params: Map<String, String>) -> Unit,
    onOpenUrl: (String) -> Unit,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: HomeViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeEffect.Navigate -> onNavigate(effect.route, effect.params)
                is HomeEffect.OpenUrl -> onOpenUrl(effect.url)
                is HomeEffect.ShowMessage -> onMessage(effect.message)
            }
        }
    }

    HomeScreen(
        state = state,
        onIntent = viewModel::dispatch,
        modifier = modifier,
    )
}
