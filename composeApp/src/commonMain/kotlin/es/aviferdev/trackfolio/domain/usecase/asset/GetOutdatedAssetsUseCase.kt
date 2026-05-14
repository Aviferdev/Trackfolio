package es.aviferdev.trackfolio.domain.usecase.asset

import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.repository.AssetRepository
import es.aviferdev.trackfolio.core.security.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

/**
 * Devuelve los activos con posiciones abiertas cuyo precio no se ha
 * actualizado dentro del intervalo configurado por el usuario.
 */
class GetOutdatedAssetsUseCase(
    private val repository: AssetRepository,
    private val appSettings: AppSettings
) {
    operator fun invoke(accountId: String): Flow<List<Asset>> {
        val intervalDays = appSettings.getInt(KEY_REMINDER_INTERVAL, DEFAULT_INTERVAL)
        val thresholdMillis = Clock.System.now().toEpochMilliseconds() -
            (intervalDays.toLong() * 24 * 60 * 60 * 1000)
        return repository.getOutdatedAssets(accountId, thresholdMillis)
    }

    companion object {
        const val KEY_REMINDER_INTERVAL = "price_reminder_interval_days"
        const val DEFAULT_INTERVAL = 14
    }
}
