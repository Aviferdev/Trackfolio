package es.aviferdev.n3to.data.datasource.price

import es.aviferdev.n3to.domain.model.ExchangeRate

/**
 * Fuente de datos remota para el tipo de cambio EUR/USD.
 */
interface ExchangeRateRemoteDataSource {
    /**
     * Obtiene el tipo de cambio EUR/USD desde una API de forex.
     */
    suspend fun fetchEurUsdRate(): Result<ExchangeRate>
}
