package com.cars24.sdui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.cars24.core.designsystem.theme.Cars24
import com.cars24.core.designsystem.theme.Cars24Theme
import com.cars24.data.page.SduiPageRepository
import com.cars24.feature.staticbaseline.StaticHomeScreen
import com.cars24.sdui.navigation.SduiNavHost
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val renderStatic = intent?.getBooleanExtra(EXTRA_STATIC_BASELINE, false) == true

        setContent {
            Cars24Theme {
                Cars24App(renderStatic = renderStatic)
            }
        }
    }

    companion object {
        const val EXTRA_STATIC_BASELINE = "com.cars24.sdui.STATIC_BASELINE"
    }
}

@Composable
private fun Cars24App(renderStatic: Boolean) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val repository: SduiPageRepository = koinInject()

    val knownPages by produceState(initialValue = emptySet<String>(), repository) {
        value = repository.availablePages()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            val contentModifier = Modifier.padding(bottom = padding.calculateBottomPadding())

            if (renderStatic) {
                StaticHomeScreen(modifier = contentModifier)
            } else {
                SduiNavHost(
                    knownPages = knownPages,
                    snackbarHostState = snackbarHostState,
                    onOpenUrl = { url ->
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }.onFailure {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Nothing on this device can open $url")
                            }
                        }
                    },
                    modifier = contentModifier,
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(Cars24.colors.brandGradient.first()),
            )
        }
    }
}
