package es.aviferdev.trackfolio.core

import es.aviferdev.trackfolio.BuildConfig

actual object AppConfig {
    actual val environment: String = BuildConfig.ENVIRONMENT
    actual val isDebug: Boolean = BuildConfig.IS_DEBUG
    actual val appDisplayName: String = BuildConfig.APP_DISPLAY_NAME
}
