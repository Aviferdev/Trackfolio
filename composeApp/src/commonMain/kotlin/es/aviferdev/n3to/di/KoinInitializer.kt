package es.aviferdev.n3to.di

import es.aviferdev.n3to.core.security.applyPendingDatabaseImport
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

fun initKoin(
    platformModule: Module,
    appDeclaration: KoinAppDeclaration = {}
) {
    // CRÍTICO: aplicar cualquier import de backup pendiente ANTES de instanciar
    // el driver de SQLite. Si no se hace aquí, el driver abre la BD vieja y
    // sobrescribirla luego provoca crashes y corrupción.
    applyPendingDatabaseImport()

    startKoin {
        appDeclaration()
        modules(
            platformModule,
            consentModule,
            databaseModule,
            repositoryModule,
            useCaseModule,
            uiModule,
            onboardingModule
        )
    }
}
