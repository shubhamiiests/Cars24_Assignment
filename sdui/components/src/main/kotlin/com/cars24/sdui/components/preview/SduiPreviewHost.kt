package com.cars24.sdui.components.preview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cars24.core.designsystem.theme.Cars24
import com.cars24.core.designsystem.theme.Cars24Theme
import com.cars24.sdui.components.Cars24Components
import com.cars24.sdui.runtime.render.LocalSduiPageState
import com.cars24.sdui.runtime.render.SduiNodeRenderer
import com.cars24.sdui.runtime.render.SduiScope
import com.cars24.sdui.schema.SduiJson
import com.cars24.sdui.schema.SduiNode

@Composable
fun SduiNodePreview(
    json: String,
    state: Map<String, String> = emptyMap(),
    padded: Boolean = true,
) {
    val node = remember(json) { SduiJson.format.decodeFromString<SduiNode>(json) }
    val registry = remember { Cars24Components.registry() }
    val scope = remember(registry) {
        SduiScope(
            registry = registry,
            pageSchemaVersion = SduiJson.SUPPORTED_SCHEMA_VERSION,
            showUnknownPlaceholders = true,
            stateProvider = { state },
            onCommand = {},
            onUnsupportedType = {},
        )
    }

    Cars24Theme {
        Surface(color = Cars24.colors.pageBackground) {
            CompositionLocalProvider(LocalSduiPageState provides state) {
                Column(modifier = if (padded) Modifier.padding(vertical = 12.dp) else Modifier) {
                    SduiNodeRenderer(node = node, scope = scope)
                }
            }
        }
    }
}
