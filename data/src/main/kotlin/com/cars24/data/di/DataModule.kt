package com.cars24.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.cars24.core.common.coroutines.DispatcherProvider
import com.cars24.core.common.coroutines.StandardDispatcherProvider
import com.cars24.core.common.network.AndroidConnectivityMonitor
import com.cars24.core.common.network.ConnectivityMonitor
import com.cars24.data.page.AssetPageDataSource
import com.cars24.data.page.FileOverridePageDataSource
import com.cars24.data.page.PageDataSource
import com.cars24.data.page.PagePayloadCache
import com.cars24.data.page.PageStateStore
import com.cars24.data.page.SduiPageRepository
import com.cars24.data.page.SduiPageRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val Context.payloadStore: DataStore<Preferences> by preferencesDataStore("sdui_payloads")
private val Context.uiStateStore: DataStore<Preferences> by preferencesDataStore("sdui_ui_state")

private val REMOTE_SOURCE = named("remote")

val dataModule = module {

    single<DispatcherProvider> { StandardDispatcherProvider() }

    single<ConnectivityMonitor> { AndroidConnectivityMonitor(androidContext()) }

    single<PageDataSource>(REMOTE_SOURCE) { AssetPageDataSource(androidContext()) }

    single { FileOverridePageDataSource(androidContext(), get(REMOTE_SOURCE)) }

    single { PagePayloadCache(androidContext().payloadStore) }

    single { PageStateStore(androidContext().uiStateStore) }

    single<SduiPageRepository> {
        SduiPageRepositoryImpl(
            remote = get(REMOTE_SOURCE),
            override = get(),
            payloadCache = get(),
            connectivity = get(),
            dispatchers = get(),
        )
    }
}
