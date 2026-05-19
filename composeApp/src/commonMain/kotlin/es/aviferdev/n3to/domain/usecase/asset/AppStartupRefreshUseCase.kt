package es.aviferdev.n3to.domain.usecase.asset

import es.aviferdev.n3to.domain.model.ExchangeRate
import es.aviferdev.n3to.domain.model.PriceRefreshResult

/**
 * Orquestador del refresco diario completo al abrir la aplicación.
 *
 * Ejecuta en orden:
 * 1. Obtener tipo de cambio EUR/USD (si no se ha hecho hoy)
 * 2. Refrescar precios de cartera (si no se ha hecho hoy)
 *
 * El orden es importante: el tipo de cambio debe estar disponible antes
 * de refrescar los precios para poder convertir USD → EUR correctamente.
 *
 * Este use case se invoca desde App.kt al iniciar la aplicación.
 */
class AppStartupRefreshUseCase(
    private val refreshExchangeRate: RefreshExchangeRateUseCase,
    private val refreshPortfolioPrices: RefreshPortfolioPricesUseCase
) {
    /**
     * Ejecuta el refresco completo.
     * @param accountId ID de la cuenta a refrescar.
     * @return Par con el resultado del tipo de cambio y el resultado de precios.
     */
    suspend operator fun invoke(accountId: String): Pair<Result<ExchangeRate>, PriceRefreshResult> {
        println("[PriceRefresh] ========== INICIO refresco diario (cuenta=$accountId) ==========")

        // 1. Primero el tipo de cambio
        val rateResult = refreshExchangeRate()

        // 2. Luego los precios (que usarán el rate recién obtenido)
        val priceResult = refreshPortfolioPrices(accountId)

        println("[PriceRefresh] ========== FIN refresco diario ==========")
        return rateResult to priceResult
    }
}
