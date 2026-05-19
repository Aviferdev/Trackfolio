package es.aviferdev.n3to.domain.repository

import es.aviferdev.n3to.domain.model.IdentifierType
import es.aviferdev.n3to.domain.model.PriceQuote

/**
 * Repositorio para obtener cotizaciones de activos financieros desde APIs externas.
 */
interface PriceRepository {
    /**
     * Obtiene la cotización actual de un instrumento financiero.
     * @param identifier ISIN, ticker o símbolo crypto.
     * @param type Tipo de identificador.
     * @return PriceQuote con el precio actual y metadata.
     */
    suspend fun getQuote(identifier: String, type: IdentifierType): Result<PriceQuote>

    /**
     * Valida que un identificador existe en el mercado y devuelve información
     * del instrumento (nombre, precio, divisa) para que el usuario confirme.
     * Es más ligero que getQuote ya que puede usar un endpoint de búsqueda.
     */
    suspend fun validateIdentifier(identifier: String, type: IdentifierType): Result<PriceQuote>

    /**
     * Verifica si la API externa está disponible (health check).
     */
    suspend fun isServiceAvailable(): Boolean
}
