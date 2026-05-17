package es.aviferdev.n3to.platform

import com.google.android.gms.tasks.Tasks
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import es.aviferdev.n3to.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class VersionRemoteConfig {
    private val config = Firebase.remoteConfig

    init {
        // Valores por defecto offline (programáticos, no vía XML por limitaciones KMP)
        config.setDefaultsAsync(
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

        // Fetch interval: 0 en debug, 12h en release
        config.setConfigSettingsAsync(
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0L else 43200L
            }
        )
    }

    actual suspend fun fetchAndActivate() {
        try {
            // Tasks.await() bloquea el hilo actual, por eso lo ejecutamos en IO
            withContext(Dispatchers.IO) {
                Tasks.await(config.fetchAndActivate())
            }
        } catch (_: Exception) {
            // Fallo de red — los defaults offline ya están cargados
        }
    }

    actual fun getString(key: String): String {
        return config.getString(key)
    }
}
