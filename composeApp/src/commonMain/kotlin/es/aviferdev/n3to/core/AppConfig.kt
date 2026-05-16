package es.aviferdev.n3to.core

/**
 * Configuración de entorno multiplataforma.
 * Cada plataforma provee su propia implementación vía expect/actual.
 *
 * Android: lee los valores desde BuildConfig (generado por product flavors)
 * iOS: valores hardcodeados (cambiar manualmente o vía script de build)
 *
 * Para añadir nuevas variables de entorno:
 * 1. Declarar la propiedad aquí (val)
 * 2. En Android: añadir buildConfigField en cada flavor en build.gradle.kts
 * 3. En iOS: añadir la propiedad en AppConfig.ios.kt
 */
expect object AppConfig {
    /** Nombre del entorno: "dev" o "prod" */
    val environment: String

    /** true si es una build de desarrollo */
    val isDebug: Boolean

    /** Nombre de la aplicación para mostrar en UI */
    val appDisplayName: String

    /** RevenueCat API key para el entorno actual */
    val revenueCatApiKey: String
}
