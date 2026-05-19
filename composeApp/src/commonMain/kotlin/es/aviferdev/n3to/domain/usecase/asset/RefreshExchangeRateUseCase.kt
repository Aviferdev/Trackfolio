package es.aviferdev.n3to.domain.usecase.asset

import es.aviferdev.n3to.domain.model.ExchangeRate
import es.aviferdev.n3to.domain.repository.ExchangeRateRepository

/**
 * Obtiene y cachea el tipo de cambio EUR/USD, respetando la frecuencia
 * diaria de actualización.
 *
 * Si ya se obtuvo hoy, devuelve el valor cacheado.
 * Si es la primera vez hoy, consulta la API y guarda en caché.
 */
class RefreshExchangeRateUseCase(
    private val exchangeRateRepository: ExchangeRateRepository,
    private val shouldRefreshToday: ShouldRefreshTodayUseCase
) {
    /**
     * Obtiene el tipo de cambio EUR/USD.
     * @return Result.success(ExchangeRate) con el tipo de cambio fresco o cacheado.
     */
    suspend operator fun invoke(): Result<ExchangeRate> {
        // Si ya se refrescó hoy, devolver caché
        if (!shouldRefreshToday(ShouldRefreshTodayUseCase.KEY_EXCHANGE_RATE_REFRESH)) {
            val cached = exchangeRateRepository.getCachedEurUsdRate()
            if (cached != null) return Result.success(cached)
        }

        // Obtener fresco de la API
        return exchangeRateRepository.fetchEurUsdRate()
            .onSuccess { rate ->
                exchangeRateRepository.saveCachedRate(rate)
                shouldRefreshToday.markRefreshedToday(
                    ShouldRefreshTodayUseCase.KEY_EXCHANGE_RATE_REFRESH
                )
            }
    }
}
