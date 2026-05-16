package es.aviferdev.n3to.core

import es.aviferdev.n3to.domain.model.VersionInfo
import es.aviferdev.n3to.domain.usecase.version.DismissVersionBannerUseCase
import es.aviferdev.n3to.domain.usecase.version.GetVersionInfoUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Orquesta la lógica de gestión de versiones de la aplicación.
 *
 * Se inyecta como singleton desde Koin. Se llama a [checkVersion] desde App.kt
 * al arrancar. La UI reacciona al [StateFlow] [status] para mostrar:
 *  - [Status.UpdateRequired]: Pantalla de bloqueo full-screen (hard block)
 *  - [Status.UpdateAvailable]: Banner no bloqueante en Home (soft update)
 *  - [Status.UpToDate]: No mostrar nada
 *  - [Status.Checking]: Estado transitorio durante el fetch
 *
 * @param getVersionInfo   Use case que obtiene la info desde Firebase RC.
 * @param dismissBanner    Use case para persistir el descarte del banner.
 * @param currentVersion   Versión instalada de la app (named("appVersion")).
 */
class VersionManager(
    private val getVersionInfo: GetVersionInfoUseCase,
    private val dismissBannerUseCase: DismissVersionBannerUseCase,
    private val currentVersion: String
) {
    /**
     * Posibles estados de la verificación de versión.
     */
    sealed class Status {
        /** Chequeando con Firebase Remote Config. */
        data object Checking : Status()

        /** La app está actualizada. */
        data object UpToDate : Status()

        /** Hay una versión más reciente disponible (no obligatoria). */
        data class UpdateAvailable(val info: VersionInfo) : Status()

        /** La versión instalada es inferior a la mínima requerida (bloqueo). */
        data class UpdateRequired(val info: VersionInfo) : Status()
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _status = MutableStateFlow<Status>(Status.Checking)
    val status: StateFlow<Status> = _status.asStateFlow()

    /**
     * Ejecuta la verificación de versión. Solo se ejecuta una vez por sesión.
     * Si ya se ha chequeado (no está en Checking), no hace nada.
     */
    fun checkVersion() {
        if (_status.value !is Status.Checking) return

        scope.launch {
            try {
                val info = getVersionInfo()
                val cmpMin = compareVersions(currentVersion, info.minVersion)
                val cmpLatest = compareVersions(currentVersion, info.latestVersion)
                val dismissed = dismissBannerUseCase.isDismissed(info.latestVersion)

                _status.value = when {
                    cmpMin < 0 -> Status.UpdateRequired(info)
                    cmpLatest < 0 && !dismissed -> Status.UpdateAvailable(info)
                    else -> Status.UpToDate
                }
            } catch (_: Exception) {
                // Sin red, timeout, o error en Firebase RC → asumir OK
                _status.value = Status.UpToDate
            }
        }
    }

    /**
     * Descarta el banner de actualización para la versión indicada.
     * El banner no se volverá a mostrar hasta que haya una versión más nueva.
     */
    fun dismissBanner(latestVersion: String) {
        dismissBannerUseCase(latestVersion)
        _status.value = Status.UpToDate
    }

    companion object {
        /**
         * Compara dos versiones semántivas (formato "X.Y.Z").
         * Soporta 1, 2 o 3 segmentos. Ignora sufijos como "-beta".
         *
         * @return < 0 si v1 < v2, > 0 si v1 > v2, 0 si iguales.
         */
        fun compareVersions(v1: String, v2: String): Int {
            val sanitize = { v: String ->
                v.split(".").map { segment ->
                    // Ignorar sufijos no numéricos (ej: "1.0.0-beta" → "1.0.0")
                    segment.takeWhile { it.isDigit() }.toIntOrNull() ?: 0
                }
            }
            val parts1 = sanitize(v1)
            val parts2 = sanitize(v2)
            val maxLen = maxOf(parts1.size, parts2.size)

            for (i in 0 until maxLen) {
                val a = parts1.getOrElse(i) { 0 }
                val b = parts2.getOrElse(i) { 0 }
                if (a != b) return a.compareTo(b)
            }
            return 0
        }
    }
}
