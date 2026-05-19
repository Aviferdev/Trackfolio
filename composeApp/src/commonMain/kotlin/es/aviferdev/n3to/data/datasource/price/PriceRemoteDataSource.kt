package es.aviferdev.n3to.data.datasource.price

import es.aviferdev.n3to.domain.model.IdentifierType
import es.aviferdev.n3to.domain.model.PriceQuote

/**
 * Fuente de datos remota para cotizaciones de activos financieros.
 * Implementación actual: Yahoo Finance.
 * Puede ser reemplazada por cualquier otra API (Twelve Data, Alpha Vantage, etc.)
 * sin afectar al resto de la aplicación.
 */
interface PriceRemoteDataSource {
    /**
     * Obtiene la cotización actual de un instrumento.
     */
    suspend fun fetchQuote(identifier: String, type: IdentifierType): Result<PriceQuote>

    /**
     * Busca/valida un identificador y devuelve información del instrumento.
     */
    suspend fun searchIdentifier(query: String): Result<List<PriceQuote>>

    /**
     * Health check de la API.
     */
    suspend fun healthCheck(): Boolean
}
