package es.aviferdev.trackfolio.di

import android.os.Build
import android.content.pm.PackageManager
import es.aviferdev.trackfolio.data.database.DatabaseDriverFactory
import es.aviferdev.trackfolio.domain.pdf.PdfReportGenerator
import es.aviferdev.trackfolio.security.AppLockManager
import es.aviferdev.trackfolio.security.AppSettings
import es.aviferdev.trackfolio.security.BalanceVisibilityManager
import es.aviferdev.trackfolio.security.BiometricAuthenticator
import es.aviferdev.trackfolio.security.DatabaseBackupManager
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
