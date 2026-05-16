package es.aviferdev.n3to.di

import es.aviferdev.n3to.core.security.AppLockManager
import es.aviferdev.n3to.core.security.AppSettings
import es.aviferdev.n3to.core.security.BalanceVisibilityManager
import es.aviferdev.n3to.core.security.BiometricAuthenticator
import es.aviferdev.n3to.core.security.DatabaseBackupManager
import es.aviferdev.n3to.data.database.DatabaseDriverFactory
import es.aviferdev.n3to.domain.pdf.PdfReportGenerator
import es.aviferdev.n3to.platform.AnalyticsTracker
import es.aviferdev.n3to.platform.CrashlyticsTracker
import es.aviferdev.n3to.platform.PurchaseManager
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
    single { AnalyticsTracker() }
    single { CrashlyticsTracker() }
    single { PurchaseManager() }
    single(named("appVersion")) {
        NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "1.0.0"
    }
}
