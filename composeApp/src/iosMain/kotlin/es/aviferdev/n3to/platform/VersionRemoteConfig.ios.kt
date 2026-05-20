@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package es.aviferdev.n3to.platform

import cocoapods.FirebaseRemoteConfig.FIRRemoteConfig
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigSettings
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class VersionRemoteConfig {
    private val config = FIRRemoteConfig.remoteConfig()

    init {
        config.setDefaults(
            mapOf(
                "app_min_version" to "1.0.0",
                "app_latest_version" to "1.0.0",
                "app_update_url_android" to "",
                "app_update_url_ios" to "",
                "app_block_title" to "Actualización requerida",
                "app_block_message" to "Debes actualizar N3to para continuar utilizando la aplicación.",
                "app_block_button_text" to "Actualizar"
            )
        )

        val settings = FIRRemoteConfigSettings()
        settings.minimumFetchInterval = 43200.0
        config.configSettings = settings
    }

    actual suspend fun fetchAndActivate() {
        try {
            suspendCancellableCoroutine<Unit> { continuation ->
                config.fetchAndActivateWithCompletionHandler { _, error ->
                    if (error != null) {
                        continuation.resume(Unit)
                    } else {
                        continuation.resume(Unit)
                    }
                }
            }
        } catch (_: Exception) {
            // Defaults offline activos en caso de error
        }
    }

    actual fun getString(key: String): String {
        return config.configValueForKey(key).stringValue ?: ""
    }
}
