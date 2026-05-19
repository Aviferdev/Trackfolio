package es.aviferdev.n3to.domain.usecase.asset

import es.aviferdev.n3to.domain.model.AssetCategoryType
import es.aviferdev.n3to.domain.model.IdentifierType
import es.aviferdev.n3to.domain.model.PriceQuote
import es.aviferdev.n3to.domain.repository.PriceRepository

/**
 * Valida un identificador (ISIN, ticker o símbolo crypto) contra la API
 * de cotizaciones para verificar que el activo existe en el mercado.
 *
 * Usa la categoría del activo para determinar el tipo de identificador
 * esperado y consulta la API. Si el identificador es válido, devuelve
 * información detallada del instrumento (nombre, precio, divisa) para
 * que el usuario pueda confirmar que es el activo correcto.
 */
class ValidateAssetIdentifierUseCase(
    private val priceRepository: PriceRepository
) {
    /**
     * Valida un identificador contra la API.
     * @param identifier ISIN, ticker o símbolo crypto normalizado.
     * @param assetCategoryId ID de la categoría del activo para determinar el tipo.
     * @return Result.success(PriceQuote) con datos del instrumento, o
     *         Result.failure con el motivo (NOT_FOUND, ERROR, etc.).
     */
    suspend operator fun invoke(
        identifier: String,
        assetCategoryId: String
    ): Result<PriceQuote> {
        val type = AssetCategoryType.identifierTypeFor(assetCategoryId)
        println("[PriceRefresh] 🔍 Validando $identifier (tipo=$type)...")
        val result = priceRepository.validateIdentifier(identifier, type)
        result.onSuccess {
            println("[PriceRefresh] ✅ $identifier válido → ${it.name} | ${it.price} ${it.currency}")
        }.onFailure { error ->
            println("[PriceRefresh] ❌ $identifier inválido → ${error.message}")
        }
        return result
    }
}
