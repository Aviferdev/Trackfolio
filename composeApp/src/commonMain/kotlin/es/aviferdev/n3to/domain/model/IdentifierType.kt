package es.aviferdev.n3to.domain.model

/**
 * Tipo de identificador usado para consultar el precio de un activo
 * en una API externa de cotizaciones.
 *
 * - [ISIN]: Código ISIN (International Securities Identification Number).
 *   Usado para acciones, ETFs, fondos de inversión.
 * - [TICKER]: Símbolo de mercado estándar.
 *   Usado para materias primas (ej: "GC=F" para oro).
 * - [CRYPTO_SYMBOL]: Símbolo de criptomoneda.
 *   Usado para criptomonedas (ej: "BTC").
 */
enum class IdentifierType {
    ISIN,
    TICKER,
    CRYPTO_SYMBOL
}
