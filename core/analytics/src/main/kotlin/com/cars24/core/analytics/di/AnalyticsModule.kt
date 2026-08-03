package com.cars24.core.analytics.di

import android.util.Log
import com.cars24.core.analytics.AnalyticsLogger
import com.cars24.core.analytics.FirebaseAnalyticsLogger
import com.cars24.core.analytics.NoOpAnalyticsLogger
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val analyticsModule = module {

    single<AnalyticsLogger> {
        val context = androidContext()
        val firebaseApp = runCatching { FirebaseApp.initializeApp(context) }.getOrNull()

        if (firebaseApp == null) {
            Log.i(
                "Cars24Analytics",
                "No Firebase app - add app/google-services.json to enable Analytics.",
            )
            NoOpAnalyticsLogger()
        } else {
            FirebaseAnalyticsLogger(FirebaseAnalytics.getInstance(context))
        }
    }
}
