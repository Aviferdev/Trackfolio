package es.aviferdev.n3to.di

import es.aviferdev.n3to.ui.common.help.HelpPreferences
import es.aviferdev.n3to.ui.common.loading.GlobalLoadingManager
import org.koin.dsl.module

val uiModule = module {
    single { GlobalLoadingManager() }
    single { HelpPreferences(get()) }
}
