package es.aviferdev.n3to.di

import android.content.pm.PackageManager
import android.os.Build
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
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val androidModule = module {
    single { DatabaseDriverFactory(androidContext()) }
    single { AppSettings(androidContext()) }
    single { BiometricAuthenticator(androidContext()) }
    single { AppLockManager(get()) }
    single { BalanceVisibilityManager(get(), get(), get()) }
    single { DatabaseBackupManager(androidContext()) }
    single { PdfReportGenerator(androidContext()) }
    single { AnalyticsTracker() }
    single { CrashlyticsTracker() }
    single { PurchaseManager() }
    single(named("appVersion")) {
        val ctx   = androidContext()
        val pm    = ctx.packageManager
        val pName = ctx.packageName
        val info  = if (Build.VERSION.SDK_INT >= 33) {
            pm.getPackageInfo(pName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(pName, 0)
        }
        info.versionName ?: "1.0.0"
    }
}
