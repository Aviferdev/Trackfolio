package es.aviferdev.trackfolio.di

import es.aviferdev.trackfolio.data.database.DatabaseDriverFactory
import es.aviferdev.trackfolio.security.AppLockManager
import es.aviferdev.trackfolio.security.BiometricAuthenticator
import es.aviferdev.trackfolio.security.DatabaseBackupManager
import org.koin.dsl.module

val iosModule = module {
    single { DatabaseDriverFactory() }
    single { BiometricAuthenticator() }
    single { AppLockManager() }
    single { DatabaseBackupManager() }
}
