package es.aviferdev.trackfolio.core

actual object AppConfig {
    actual val environment: String = "prod"
    actual val isDebug: Boolean = false
    actual val appDisplayName: String = "Trackfolio"
}
