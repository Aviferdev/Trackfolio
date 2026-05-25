package es.aviferdev.n3to.core

actual object AppConfig {
    actual val environment: String = "prod"
    actual val isDebug: Boolean = false
    actual val appDisplayName: String = "N3to"
}
