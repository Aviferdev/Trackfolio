package es.aviferdev.trackfolio.di

import es.aviferdev.trackfolio.data.database.DatabaseDriverFactory
import es.aviferdev.trackfolio.domain.pdf.PdfReportGenerator
import es.aviferdev.trackfolio.security.AppLockManager
import es.aviferdev.trackfolio.security.AppSettings
import es.aviferdev.trackfolio.security.BalanceVisibilityManager
import es.aviferdev.trackfolio.security.BiometricAuthenticator
import es.aviferdev.trackfolio.security.DatabaseBackupManager
import org.koin.dsl.module

val iosModule = module {
    single { DatabaseDriverFactory() }
    single { AppSettings() }
    single { BiometricAuthenticator() }
    single { AppLockManager(get()) }
    single { BalanceVisibilityManager(get()) }
    single { DatabaseBackupManager() }
    single { PdfReportGenerator() }
}
