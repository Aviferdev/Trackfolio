package es.aviferdev.n3to.platform

/**
 * Wrapper multiplataforma de Firebase Remote Config.
 *
 * Sigue el mismo patrón [expect/actual] que [AnalyticsTracker] y [CrashlyticsTracker],
 * delegando en el SDK oficial de cada plataforma:
 *  - Android: com.google.firebase:firebase-config-ktx
 *  - iOS:     FirebaseRemoteConfig (CocoaPods)
 *
 * Los valores por defecto offline se configuran en cada `actual class`:
 *  - Android: res/xml/remote_config_defaults.xml
 *  - iOS:     `setDefaults()` programático en el init
 */
expect class VersionRemoteConfig {

    /**
     * Inicializa (si no lo está) y hace fetch+activate de los parámetros remotos.
     * En caso de error de red, los valores por defecto offline permanecen activos.
     */
    suspend fun fetchAndActivate()

    /**
     * Retorna el valor String de un parámetro.
     * Si no se ha podido hacer fetch, devuelve el valor por defecto offline.
     */
    fun getString(key: String): String
}
