package es.aviferdev.n3to.core

import es.aviferdev.n3to.BuildConfig

actual object AppConfig {
    actual val environment: String = BuildConfig.ENVIRONMENT
    actual val isDebug: Boolean = BuildConfig.IS_DEBUG
    actual val appDisplayName: String = BuildConfig.APP_DISPLAY_NAME
    actual val revenueCatApiKey: String = BuildConfig.REVENUECAT_API_KEY
}
