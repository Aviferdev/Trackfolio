package es.aviferdev.n3to.domain.model

/**
 * Resultado de una consulta de cotización a una API externa.
 *
 * @property identifier Identificador consultado (ISIN, ticker o símbolo crypto).
 * @property identifierType Tipo de identificador consultado.
 * @property price Precio de cotización en la moneda original del mercado.
 * @property currency Código ISO 4217 de la moneda (ej: "EUR", "USD", "GBP").
 * @property name Nombre del instrumento financiero devuelto por la API.
 * @property exchange Mercado/bolsa donde cotiza (ej: "XETRA", "NASDAQ"). Puede ser null.
 * @property retrievedAt Timestamp epoch millis de la consulta.
 */
data class PriceQuote(
    val identifier: String,
    val identifierType: IdentifierType,
    val price: Double,
    val currency: String,
    val name: String?,
    val exchange: String?,
    val retrievedAt: Long
)
