package es.aviferdev.n3to.domain.usecase.asset

import es.aviferdev.n3to.domain.repository.ExchangeRateRepository

/**
 * Convierte un precio de una divisa extranjera a EUR usando el tipo de cambio
 * cacheado localmente.
 *
 * Actualmente solo soporta conversión USD → EUR, que es el caso más común
 * para activos cotizados en mercados americanos (NASDAQ, NYSE).
 */
class ConvertPriceToEurUseCase(
    private val exchangeRateRepository: ExchangeRateRepository
) {
    /**
     * Convierte el precio a EUR si está en otra divisa.
     * @param price Precio en la moneda original.
     * @param currency Código ISO de la moneda (ej: "USD", "EUR").
     * @return Pair(priceInEur, wasConverted) donde wasConverted es true si se
     *         aplicó conversión, false si el precio ya estaba en EUR.
     */
    operator fun invoke(price: Double, currency: String): Pair<Double, Boolean> {
        if (currency.equals("EUR", ignoreCase = true)) {
            return price to false
        }
        if (!currency.equals("USD", ignoreCase = true)) {
            // De momento solo soportamos USD → EUR
            return price to false
        }
        val rate = exchangeRateRepository.getCachedEurUsdRate()
        if (rate == null) {
            // No hay tipo de cambio disponible, devolver precio original
            return price to false
        }
        return (price * rate.eurPerUsd) to true
    }
}
