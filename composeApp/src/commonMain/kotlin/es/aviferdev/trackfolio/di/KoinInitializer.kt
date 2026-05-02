package es.aviferdev.trackfolio.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

fun initKoin(
    platformModule: Module,
    appDeclaration: KoinAppDeclaration = {}
) {
    startKoin {
        appDeclaration()
        modules(
            platformModule,
            databaseModule,
            repositoryModule,
            useCaseModule
        )
    }
}
