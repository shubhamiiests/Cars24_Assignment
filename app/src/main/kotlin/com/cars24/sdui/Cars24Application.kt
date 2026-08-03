package com.cars24.sdui

import android.app.Application
import com.cars24.core.analytics.di.analyticsModule
import com.cars24.core.common.perf.StartupTrace
import com.cars24.data.di.dataModule
import com.cars24.feature.home.di.homeModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class Cars24Application : Application() {

    override fun onCreate() {
        super.onCreate()

        StartupTrace.mark(MARK_APPLICATION_CREATED)

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@Cars24Application)
            modules(dataModule, analyticsModule, homeModule)
        }
    }

    private companion object {
        const val MARK_APPLICATION_CREATED = "application_created"
    }
}
