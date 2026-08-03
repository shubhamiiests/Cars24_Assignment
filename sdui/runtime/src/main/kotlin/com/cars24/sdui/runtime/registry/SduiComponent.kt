package com.cars24.sdui.runtime.registry

import androidx.compose.runtime.Composable
import com.cars24.sdui.runtime.render.SduiScope
import com.cars24.sdui.schema.SduiNode

interface SduiComponent {

    val type: String

    @Composable
    fun Render(node: SduiNode, scope: SduiScope)
}

class ComponentRegistry(components: List<SduiComponent>) {

    private val byType: Map<String, SduiComponent> = buildMap(components.size) {
        components.forEach { component ->
            val clash = put(component.type, component)
            require(clash == null) {
                "Two components claim type '${component.type}': " +
                    "${clash!!::class.simpleName} and ${component::class.simpleName}"
            }
        }
    }

    fun find(type: String): SduiComponent? = byType[type]

    val supportedTypes: Set<String> get() = byType.keys
}
