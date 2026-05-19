package es.aviferdev.n3to.domain.model

/**
 * Tipo de cambio EUR/USD obtenido de una API de forex.
 *
 * @property eurPerUsd Cantidad de euros que vale 1 USD (ej: 0.92).
 *   Para convertir USD → EUR: multiplicar por este valor.
 * @property usdPerEur Cantidad de dólares que vale 1 EUR (ej: 1.09).
 *   Para convertir EUR → USD: multiplicar por este valor.
 * @property retrievedAt Timestamp epoch millis de la consulta.
 */
data class ExchangeRate(
    val eurPerUsd: Double,
    val usdPerEur: Double,
    val retrievedAt: Long
) {
    companion object {
        /**
         * Crea un [ExchangeRate] a partir del precio EUR/USD directo.
         * @param eurUsdPrice Precio del par EUR/USD (cuántos USD vale 1 EUR).
         */
        fun fromEurUsdPair(eurUsdPrice: Double, retrievedAt: Long): ExchangeRate {
            require(eurUsdPrice > 0) { "EUR/USD price must be positive" }
            return ExchangeRate(
                eurPerUsd = 1.0 / eurUsdPrice,  // 1 USD = 1/(EUR/USD) EUR
                usdPerEur = eurUsdPrice,        // 1 EUR = (EUR/USD) USD
                retrievedAt = retrievedAt
            )
        }
    }
}
