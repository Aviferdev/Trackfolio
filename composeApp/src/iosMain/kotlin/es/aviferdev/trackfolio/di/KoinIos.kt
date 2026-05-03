package es.aviferdev.trackfolio.di

import org.koin.core.context.startKoin

fun initKoinIos() {
    startKoin {
        modules(
            iosModule,
            databaseModule,
            repositoryModule,
            useCaseModule
        )
    }
}
