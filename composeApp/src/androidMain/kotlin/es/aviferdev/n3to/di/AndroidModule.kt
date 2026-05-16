package es.aviferdev.n3to.di

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import es.aviferdev.n3to.platform.VersionRemoteConfig
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
    single { VersionRemoteConfig() }
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
    single(named("openStore")) {
        {
            try {
                val ctx = androidContext()
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("market://details?id=es.aviferdev.n3to")
                    setPackage("com.android.vending")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                ctx.startActivity(intent)
            } catch (_: Exception) {
                // Si no hay Play Store, abrir en navegador
                val ctx = androidContext()
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data =
                        Uri.parse("https://play.google.com/store/apps/details?id=es.aviferdev.n3to")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                ctx.startActivity(intent)
            }
        } as () -> Unit
    }
    single(named("shareApp")) {
        {
            val ctx = androidContext()
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "N3to - Controla tus finanzas")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "📱 Descarga N3to y controla tus finanzas personales:\n" +
                            "https://n3to.avifer.dev"
                )
            }
            val chooser = Intent.createChooser(shareIntent, "Compartir N3to")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(chooser)
        } as () -> Unit
    }
}
