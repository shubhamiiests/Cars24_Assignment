package com.cars24.sdui.runtime.render

import android.util.Log
import com.cars24.sdui.schema.SduiNode

object SduiLog {

    const val TAG = "Cars24Sdui"

    fun unsupportedType(node: SduiNode) {
        Log.w(TAG, "No component registered for type='${node.type}' (id=${node.id})")
    }

    fun tooNew(node: SduiNode, supported: Int) {
        Log.w(
            TAG,
            "Section id=${node.id} type=${node.type} needs schema ${node.minSchemaVersion}, " +
                "this build supports $supported",
        )
    }

    fun propsFailure(node: SduiNode, cause: Throwable) {
        Log.w(TAG, "Could not decode props for id=${node.id} type=${node.type}: ${cause.message}")
    }

    fun renderFailure(node: SduiNode, cause: Throwable) {
        Log.e(TAG, "Component threw while rendering id=${node.id} type=${node.type}", cause)
    }
}
