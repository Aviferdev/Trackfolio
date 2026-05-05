package es.aviferdev.trackfolio.di

import es.aviferdev.trackfolio.data.database.DatabaseDriverFactory
import es.aviferdev.trackfolio.security.AppLockManager
import es.aviferdev.trackfolio.security.BiometricAuthenticator
import es.aviferdev.trackfolio.security.DatabaseBackupManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    single { DatabaseDriverFactory(androidContext()) }
    single { BiometricAuthenticator(androidContext()) }
    single { AppLockManager() }
    single { DatabaseBackupManager(androidContext()) }
}
