package es.aviferdev.n3to.data.repository.price

import es.aviferdev.n3to.core.security.AppSettings
import es.aviferdev.n3to.data.datasource.price.ExchangeRateRemoteDataSource
import es.aviferdev.n3to.domain.model.ExchangeRate
import es.aviferdev.n3to.domain.repository.ExchangeRateRepository

/**
 * Implementación de [ExchangeRateRepository] que obtiene el tipo de cambio
 * desde una API remota y lo cachea en [AppSettings] para uso offline.
 *
 * Claves de cache:
 * - "eur_usd_rate": Valor del tipo de cambio (1 USD = X EUR) como Double.
 * - "eur_usd_rate_date": Timestamp de la última actualización.
 */
class ExchangeRateRepositoryImpl(
    private val remoteDataSource: ExchangeRateRemoteDataSource,
    private val appSettings: AppSettings
) : ExchangeRateRepository {

    override suspend fun fetchEurUsdRate(): Result<ExchangeRate> {
        return remoteDataSource.fetchEurUsdRate()
    }

    override fun getCachedEurUsdRate(): ExchangeRate? {
        val rateStr = appSettings.getString("eur_usd_rate", "")
        if (rateStr.isBlank()) return null
        val rate = rateStr.toDoubleOrNull() ?: return null
        val date = appSettings.getLong("eur_usd_rate_date", 0L)
        if (date == 0L) return null
        return ExchangeRate(
            eurPerUsd = rate,
            usdPerEur = 1.0 / rate,
            retrievedAt = date
        )
    }

    override suspend fun saveCachedRate(rate: ExchangeRate) {
        appSettings.putString("eur_usd_rate", rate.eurPerUsd.toString())
        appSettings.putLong("eur_usd_rate_date", rate.retrievedAt)
    }
}
