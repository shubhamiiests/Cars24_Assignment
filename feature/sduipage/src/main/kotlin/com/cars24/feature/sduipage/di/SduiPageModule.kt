package com.cars24.feature.sduipage.di

import com.cars24.feature.sduipage.SduiPageViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val sduiPageModule = module {
    @Suppress("UNCHECKED_CAST")
    viewModel { (pageId: String, routeParams: Map<String, String>) ->
        SduiPageViewModel(
            pageId = pageId,
            routeParams = routeParams,
            repository = get(),
            pageStateStore = get(),
            analytics = get(),
        )
    }
}
