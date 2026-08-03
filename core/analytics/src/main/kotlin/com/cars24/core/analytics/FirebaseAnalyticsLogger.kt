package com.cars24.core.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseAnalyticsLogger(
    private val firebaseAnalytics: FirebaseAnalytics,
) : AnalyticsLogger {

    override fun logEvent(name: String, params: Map<String, String>) {
        firebaseAnalytics.logEvent(name.take(MAX_NAME_LENGTH), params.toBundle())
    }

    override fun logScreenView(screenName: String) {
        firebaseAnalytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
            },
        )
    }

    override val isActive: Boolean = true

    private fun Map<String, String>.toBundle(): Bundle = Bundle(size).also { bundle ->
        forEach { (key, value) ->
            bundle.putString(key.take(MAX_NAME_LENGTH), value.take(MAX_VALUE_LENGTH))
        }
    }

    private companion object {
        const val MAX_NAME_LENGTH = 40
        const val MAX_VALUE_LENGTH = 100
    }
}
