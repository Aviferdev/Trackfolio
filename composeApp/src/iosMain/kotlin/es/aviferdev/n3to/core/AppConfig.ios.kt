package es.aviferdev.n3to.core

/**
 * Implementación iOS de AppConfig.
 *
 * ⚠️ Cambiar según el entorno antes de compilar el framework:
 *
 *   Debug  → environment="dev",  isDebug=true,  appDisplayName="N3to DEV"
 *   Release → environment="prod", isDebug=false, appDisplayName="N3to"
 */
actual object AppConfig {
    actual val environment: String = "dev"
    actual val isDebug: Boolean = true
    actual val appDisplayName: String = "N3to DEV"
}
