package es.aviferdev.n3to.domain.repository

import es.aviferdev.n3to.domain.model.ExchangeRate

/**
 * Repositorio para obtener y cachear el tipo de cambio EUR/USD.
 */
interface ExchangeRateRepository {
    /**
     * Obtiene el tipo de cambio EUR/USD fresco desde la API.
     */
    suspend fun fetchEurUsdRate(): Result<ExchangeRate>

    /**
     * Obtiene el último tipo de cambio cacheado localmente.
     * @return ExchangeRate si hay uno en caché, null si nunca se ha obtenido.
     */
    fun getCachedEurUsdRate(): ExchangeRate?

    /**
     * Guarda el tipo de cambio en el caché local (AppSettings).
     */
    suspend fun saveCachedRate(rate: ExchangeRate)
}
