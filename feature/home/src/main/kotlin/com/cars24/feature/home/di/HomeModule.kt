package com.cars24.feature.home.di

import com.cars24.feature.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    viewModel { HomeViewModel(repository = get(), pageStateStore = get(), analytics = get()) }
}
