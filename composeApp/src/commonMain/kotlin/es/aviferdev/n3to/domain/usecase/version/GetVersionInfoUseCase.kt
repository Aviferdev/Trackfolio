package es.aviferdev.n3to.domain.usecase.version

import es.aviferdev.n3to.domain.model.VersionInfo
import es.aviferdev.n3to.platform.VersionRemoteConfig

/**
 * Obtiene la información de versiones desde Firebase Remote Config.
 *
 * Hace fetch+activate de los parámetros remotos y construye un [VersionInfo].
 * Si no hay red, usa los valores por defecto offline cargados en [VersionRemoteConfig].
 */
class GetVersionInfoUseCase(
    private val remoteConfig: VersionRemoteConfig
) {
    suspend operator fun invoke(): VersionInfo {
        remoteConfig.fetchAndActivate()
        return VersionInfo(
            minVersion = remoteConfig.getString("app_min_version"),
            latestVersion = remoteConfig.getString("app_latest_version"),
            updateUrlAndroid = remoteConfig.getString("app_update_url_android"),
            updateUrlIos = remoteConfig.getString("app_update_url_ios"),
            blockTitle = remoteConfig.getString("app_block_title"),
            blockMessage = remoteConfig.getString("app_block_message"),
            blockButtonText = remoteConfig.getString("app_block_button_text")
        )
    }
}
