package com.cars24.sdui.runtime.render

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import com.cars24.sdui.runtime.action.SduiActionParser
import com.cars24.sdui.runtime.action.SduiCommand
import com.cars24.sdui.runtime.registry.ComponentRegistry
import com.cars24.sdui.schema.SduiJson
import com.cars24.sdui.schema.SduiNode
import com.cars24.sdui.schema.SduiTemplate
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

@Immutable
class SduiScope(
    val registry: ComponentRegistry,
    val state: Map<String, String>,
    val pageSchemaVersion: Int,
    val showUnknownPlaceholders: Boolean,
    private val onCommand: (SduiCommand) -> Unit,
    private val onUnsupportedType: (String) -> Unit,
) {

    fun dispatch(node: SduiNode, trigger: String) {
        val action = node.actions[trigger] ?: return
        onCommand(SduiActionParser.parse(action, state))
    }

    fun dispatch(command: SduiCommand) = onCommand(command)

    fun reportUnsupportedType(type: String) = onUnsupportedType(type)

    fun hasAction(node: SduiNode, trigger: String): Boolean = node.actions.containsKey(trigger)
}

object SduiTriggers {
    const val ON_CLICK = "onClick"
    const val ON_SELECT = "onSelect"
    const val ON_APPEAR = "onAppear"
}

@Composable
inline fun <reified T> rememberProps(node: SduiNode, scope: SduiScope): T? {
    val needsResolution = remember(node.props) { hasPlaceholder(node.props) }
    val stateForKey = if (needsResolution) scope.state else EMPTY_STATE

    val resolved = remember(node.props, stateForKey) {
        if (needsResolution) {
            SduiTemplate.resolve(node.props, stateForKey) as JsonObject
        } else {
            node.props
        }
    }

    return remember(resolved) {
        runCatching { SduiJson.format.decodeFromJsonElement<T>(resolved) }
            .onFailure { SduiLog.propsFailure(node, it) }
            .getOrNull()
    }
}

@PublishedApi
internal val EMPTY_STATE: Map<String, String> = emptyMap()


@PublishedApi
internal fun hasPlaceholder(props: JsonObject): Boolean =
    props.isNotEmpty() && props.toString().contains("{{")
