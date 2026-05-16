package es.aviferdev.n3to.domain.usecase.version

import es.aviferdev.n3to.core.security.AppSettings

/**
 * Gestiona el descarte del banner de actualización disponible.
 *
 * Cuando el usuario descarta el banner, se persiste la [latestVersion] descartada
 * para no volver a mostrarle el banner hasta que haya una versión todavía más reciente.
 */
class DismissVersionBannerUseCase(
    private val appSettings: AppSettings
) {
    companion object {
        private const val KEY_DISMISSED_VERSION = "dismissed_version_banner"
    }

    /**
     * Persiste que el usuario descartó el banner para esta [latestVersion].
     */
    operator fun invoke(latestVersion: String) {
        appSettings.putString(KEY_DISMISSED_VERSION, latestVersion)
    }

    /**
     * Retorna true si el usuario ya descartó el banner para esta [latestVersion].
     */
    fun isDismissed(latestVersion: String): Boolean {
        return appSettings.getString(KEY_DISMISSED_VERSION, "") == latestVersion
    }
}
