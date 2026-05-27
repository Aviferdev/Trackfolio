package es.aviferdev.n3to.domain.usecase.asset

import es.aviferdev.n3to.domain.model.AppCurrency
import es.aviferdev.n3to.domain.model.AssetCategoryType
import es.aviferdev.n3to.domain.model.ExchangeRate
import es.aviferdev.n3to.domain.model.PriceRefreshResult
import es.aviferdev.n3to.domain.repository.AssetRepository
import es.aviferdev.n3to.domain.repository.ExchangeRateRepository
import es.aviferdev.n3to.domain.repository.PriceRepository
import kotlinx.coroutines.flow.first

/**
 * Refresca los precios de todos los activos cotizables de una cuenta.
 *
 * Itera sobre los activos que tienen ISIN y pertenecen a categorías
 * cotizables (Acciones, ETFs, Fondos, Cripto, Materias primas).
 * Para cada activo, consulta la API, convierte USD→EUR si es necesario,
 * y actualiza el precio en BD con histórico.
 *
 * Respeta rate-limiting: 500ms entre requests para no saturar la API gratuita.
 */
class RefreshPortfolioPricesUseCase(
    private val assetRepository: AssetRepository,
    private val priceRepository: PriceRepository,
    private val exchangeRateRepository: ExchangeRateRepository,
    private val updateAssetCurrentPrice: UpdateAssetCurrentPriceUseCase,
    private val convertPriceToEur: ConvertPriceToEurUseCase,
    private val shouldRefreshToday: ShouldRefreshTodayUseCase
) {
    companion object {
        /** Pausa entre requests para evitar rate limiting */
        private const val RATE_LIMIT_DELAY_MS = 500L

        /** Tiempo máximo de espera por request */
        private const val REQUEST_TIMEOUT_MS = 10_000L
    }

    /**
     * Ejecuta el refresco de precios para todos los activos cotizables de la cuenta.
     * @param accountId ID de la cuenta cuyos activos refrescar.
     * @return PriceRefreshResult con el resumen de la operación.
     */
    suspend operator fun invoke(accountId: String): PriceRefreshResult {
        // Verificar si ya se hizo hoy
        if (!shouldRefreshToday()) {
            return PriceRefreshResult()
        }

        // Obtener activos cotizables con ISIN
        val allAssets = assetRepository.getQuotableAssets(accountId).first()
        val exchangeRate = exchangeRateRepository.getCachedEurUsdRate()
        println("[PriceRefresh] 🚀 Iniciando refresco de ${allAssets.size} activos")

        var updated = 0
        var failed = 0
        var converted = 0
        val notFound = mutableListOf<String>()

        for ((index, asset) in allAssets.withIndex()) {
            val isin = asset.isin ?: continue
            if (isin.isBlank()) continue
            val categoryId = asset.assetCategoryId ?: continue
            val type = AssetCategoryType.identifierTypeFor(categoryId)

            println("[PriceRefresh] 🔄 ${index + 1}/${allAssets.size} ${asset.ticker} ($isin)")

            // Pausa entre requests
            if (index > 0) {
                kotlinx.coroutines.delay(RATE_LIMIT_DELAY_MS)
            }

            val result = priceRepository.getQuote(isin, type)
            result.onSuccess { quote ->
                val (priceEur, wasConverted) = convertPriceToEur(quote.price, quote.currency)
                if (wasConverted) converted++

                // Actualizar precio + histórico + marcar como AUTO
                updateAssetCurrentPrice(asset.id, priceEur, quote.retrievedAt, categoryId)
                assetRepository.updatePriceSource(
                    asset.id,
                    es.aviferdev.n3to.domain.model.PriceSource.AUTO
                )
                assetRepository.markIsinValidationError(asset.id, null)
                updated++
                println("[PriceRefresh] ✅ ${asset.ticker} → ${priceEur} ${AppCurrency.EUR.symbol} ${if (wasConverted) "(convertido de ${quote.currency})" else ""}")
            }.onFailure { error ->
                val msg = error.message ?: ""
                if (msg.contains("Not Found", ignoreCase = true) ||
                    msg.contains("404", ignoreCase = true)
                ) {
                    notFound.add(isin)
                    assetRepository.markIsinValidationError(asset.id, "NOT_FOUND")
                    println("[PriceRefresh] ⚠️ ${asset.ticker} → ISIN no encontrado")
                } else {
                    assetRepository.markIsinValidationError(asset.id, "API_ERROR")
                    println("[PriceRefresh] ❌ ${asset.ticker} → $msg")
                }
                failed++
            }
        }

        // Marcar que ya se refrescó hoy
        shouldRefreshToday.markRefreshedToday()

        val result = PriceRefreshResult(
            totalQuotableAssets = allAssets.size,
            updatedCount = updated,
            failedCount = failed,
            notFoundIdentifiers = notFound,
            skippedCount = 0,
            exchangeRateUsed = exchangeRate,
            convertedCount = converted
        )
        println("[PriceRefresh] 🏁 Refresco completado: $updated actualizados, $failed fallos, $converted convertidos, ${notFound.size} no encontrados")
        return result
    }
}
