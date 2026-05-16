package es.aviferdev.n3to.domain.model

/**
 * Información de versiones obtenida desde Firebase Remote Config.
 *
 * @property minVersion       Versión mínima requerida para usar la app (semver, ej: "1.2.0").
 * @property latestVersion    Última versión publicada (semver, ej: "1.3.0").
 * @property updateUrlAndroid URL para abrir Play Store (market:// o https://).
 * @property updateUrlIos     URL para abrir App Store (itms-apps:// o https://).
 */
data class VersionInfo(
    val minVersion: String,
    val latestVersion: String,
    val updateUrlAndroid: String,
    val updateUrlIos: String
)
