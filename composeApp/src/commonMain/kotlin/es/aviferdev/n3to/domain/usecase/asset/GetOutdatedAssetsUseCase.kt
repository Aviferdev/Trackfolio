package es.aviferdev.n3to.domain.usecase.asset

import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.repository.AssetRepository
import es.aviferdev.n3to.core.security.AppSettings
import kotlinx.coroutines.flow.Flow

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
        val thresholdMillis = nowMillis() -
                (intervalDays.toLong() * 24 * 60 * 60 * 1000)
        return repository.getOutdatedAssets(accountId, thresholdMillis)
    }

    companion object {
        const val KEY_REMINDER_INTERVAL = "price_reminder_interval_days"
        const val DEFAULT_INTERVAL = 14
    }
}
