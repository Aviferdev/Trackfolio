package es.aviferdev.trackfolio.di

import es.aviferdev.trackfolio.ui.common.loading.GlobalLoadingManager
import org.koin.dsl.module

val uiModule = module {
    single { GlobalLoadingManager() }
}
