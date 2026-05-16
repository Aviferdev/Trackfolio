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
import es.aviferdev.n3to.platform.VersionRemoteConfig
import org.koin.core.qualifier.named
import org.koin.dsl.module
import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController

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
    single { VersionRemoteConfig() }
    single(named("appVersion")) {
        NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "1.0.0"
    }
    single(named("openStore")) {
        {
            val url = NSURL.URLWithString("itms-apps://apps.apple.com/app/idXXXXXXXXX")
            if (url != null) {
                UIApplication.sharedApplication.openURL(url)
            }
        } as () -> Unit
    }
    single(named("shareApp")) {
        {
            val text = "📱 Descarga N3to y controla tus finanzas personales:\n" +
                    "https://n3to.avifer.dev"
            val activityVC = UIActivityViewController(
                activityItems = listOf(text),
                applicationActivities = null
            )
            val windowScene = UIApplication.sharedApplication
                .connectedScenes
                .filterIsInstance<UIWindowScene>()
                .firstOrNull() ?: return@single
            val keyWindow = windowScene.windows
                .filterIsInstance<UIWindow>()
                .firstOrNull { it.isKeyWindow() }
                ?: windowScene.windows.filterIsInstance<UIWindow>().firstOrNull() ?: return@single
            val presenter = keyWindow.rootViewController ?: return@single
            val popover = activityVC.popoverPresentationController
            if (popover != null) {
                popover.sourceView = presenter.view
            }
            presenter.presentViewController(activityVC, animated = true, completion = null)
        } as () -> Unit
    }
}
