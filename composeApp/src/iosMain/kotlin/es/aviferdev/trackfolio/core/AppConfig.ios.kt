package es.aviferdev.trackfolio.core

/**
 * Implementación iOS de AppConfig.
 *
 * ⚠️ Cambiar según el entorno antes de compilar el framework:
 *
 *   Debug  → environment="dev",  isDebug=true,  appDisplayName="Trackfolio DEV"
 *   Release → environment="prod", isDebug=false, appDisplayName="Trackfolio"
 */
actual object AppConfig {
    actual val environment: String = "dev"
    actual val isDebug: Boolean = true
    actual val appDisplayName: String = "Trackfolio DEV"
}
