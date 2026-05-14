package es.aviferdev.trackfolio.di

import es.aviferdev.trackfolio.data.database.DatabaseDriverFactory
import es.aviferdev.trackfolio.domain.pdf.PdfReportGenerator
import es.aviferdev.trackfolio.core.security.AppLockManager
import es.aviferdev.trackfolio.core.security.AppSettings
import es.aviferdev.trackfolio.core.security.BalanceVisibilityManager
import es.aviferdev.trackfolio.core.security.BiometricAuthenticator
import es.aviferdev.trackfolio.core.security.DatabaseBackupManager
import org.koin.core.qualifier.named
import org.koin.dsl.module
import platform.Foundation.NSBundle

val iosModule = module {
    single { DatabaseDriverFactory() }
    single { AppSettings() }
    single { BiometricAuthenticator() }
    single { AppLockManager(get()) }
    single { BalanceVisibilityManager(get(), get(), get()) }
    single { DatabaseBackupManager() }
    single { PdfReportGenerator() }
    single(named("appVersion")) {
        NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "1.0.0"
    }
}
