package com.cars24.sdui.runtime.action

import com.cars24.sdui.schema.SduiAction
import com.cars24.sdui.schema.SduiNode
import com.cars24.sdui.schema.SduiTemplate

sealed interface SduiCommand {

    data class Navigate(val route: String, val params: Map<String, String>) : SduiCommand

    data class OpenSheet(
        val sheetId: String,
        val title: String?,
        val content: List<SduiNode>,
    ) : SduiCommand

    data object DismissSheet : SduiCommand

    data class SetState(val key: String, val value: String) : SduiCommand

    data class Track(val event: String, val params: Map<String, String>) : SduiCommand

    data class OpenUrl(val url: String) : SduiCommand

    data object Refresh : SduiCommand

    data class Batch(val commands: List<SduiCommand>) : SduiCommand

    data class Unsupported(val type: String) : SduiCommand
}

object SduiActionType {
    const val NAVIGATE = "navigate"
    const val OPEN_BOTTOM_SHEET = "open_bottom_sheet"
    const val DISMISS_BOTTOM_SHEET = "dismiss_bottom_sheet"
    const val SET_STATE = "set_state"
    const val TRACK_EVENT = "track_event"
    const val OPEN_URL = "open_url"
    const val REFRESH = "refresh"
}

object SduiActionParser {

    fun parse(action: SduiAction, state: Map<String, String>): SduiCommand {
        val params = SduiTemplate.resolve(action.params, state)

        val primary = when (action.type) {
            SduiActionType.NAVIGATE -> SduiCommand.Navigate(
                route = params["route"].orEmpty(),
                params = params - "route",
            )

            SduiActionType.OPEN_BOTTOM_SHEET -> SduiCommand.OpenSheet(
                sheetId = params["sheetId"] ?: action.type,
                title = params["title"],
                content = action.content,
            )

            SduiActionType.DISMISS_BOTTOM_SHEET -> SduiCommand.DismissSheet

            SduiActionType.SET_STATE -> {
                val key = params["key"]
                val value = params["value"]
                if (key.isNullOrEmpty() || value == null) {
                    SduiCommand.Unsupported("${action.type}(malformed)")
                } else {
                    SduiCommand.SetState(key, value)
                }
            }

            SduiActionType.TRACK_EVENT -> SduiCommand.Track(
                event = params["name"].orEmpty(),
                params = params - "name",
            )

            SduiActionType.OPEN_URL -> SduiCommand.OpenUrl(params["url"].orEmpty())

            SduiActionType.REFRESH -> SduiCommand.Refresh

            else -> SduiCommand.Unsupported(action.type)
        }

        if (action.then.isEmpty()) return primary

        return SduiCommand.Batch(
            listOf(primary) + action.then.map { parse(it, state) },
        )
    }
}
