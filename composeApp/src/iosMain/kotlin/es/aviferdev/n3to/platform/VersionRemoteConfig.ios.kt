package es.aviferdev.n3to.platform

import cocoapods.FirebaseRemoteConfig.FIRRemoteConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class VersionRemoteConfig {
    private val config = FIRRemoteConfig.remoteConfig()

    init {
        // Valores por defecto offline para cuando no hay conexión
        config.setDefaults(
            mapOf(
                "app_min_version" to "1.0.0",
                "app_latest_version" to "1.0.0",
                "app_update_url_android" to "",
                "app_update_url_ios" to ""
            )
        )

        // Fetch interval mínimo: 12 horas
        val settings = FIRRemoteConfig.remoteConfigSettings()
        settings.minimumFetchInterval = 43200.0
        config.configSettings = settings
    }

    actual suspend fun fetchAndActivate() {
        try {
            suspendCancellableCoroutine<Unit> { continuation ->
                config.fetchAndActivateWithCompletionHandler { _, error ->
                    if (error != null) {
                        // Fallo de red — los defaults offline siguen activos
                        continuation.resume(Unit)
                    } else {
                        continuation.resume(Unit)
                    }
                }
            }
        } catch (_: Exception) {
            // Cualquier error: los defaults offline están cargados
        }
    }

    actual fun getString(key: String): String {
        return config.configValueForKey(key).stringValue ?: ""
    }
}
