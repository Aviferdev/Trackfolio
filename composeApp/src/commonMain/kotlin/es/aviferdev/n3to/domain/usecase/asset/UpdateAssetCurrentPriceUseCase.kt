package es.aviferdev.n3to.domain.usecase.asset

import com.benasher44.uuid.uuid4
import es.aviferdev.n3to.domain.model.AssetCategoryType
import es.aviferdev.n3to.domain.model.AssetPriceHistory
import es.aviferdev.n3to.domain.repository.AssetPriceHistoryRepository
import es.aviferdev.n3to.domain.repository.AssetRepository

/**
 * Actualiza el precio actual y la fecha de última actualización de un activo.
 * Además, registra una entrada en el histórico de precios para poder trazar
 * la evolución del portfolio a lo largo del tiempo.
 *
 * Nota: Los activos de Renta Fija no registran histórico de precios ya que
 * su valor se determina por el nominal y no por cotizaciones de mercado.
 */
class UpdateAssetCurrentPriceUseCase(
    private val repository: AssetRepository,
    private val priceHistoryRepository: AssetPriceHistoryRepository
) {
    suspend operator fun invoke(
        assetId: String,
        price: Double,
        updatedAt: Long,
        assetCategoryId: String? = null
    ): Result<Unit> {
        val updateResult = repository.updateCurrentPrice(assetId, price, updatedAt)
        if (updateResult.isSuccess) {
            // Solo registrar histórico de precios para activos de inversión (no Renta Fija)
            // Se verifica usando el categoryId proporcionado o consultando el repositorio
            val shouldRecordHistory =
                assetCategoryId?.let { !AssetCategoryType.isFixedIncome(it) } ?: true
            if (shouldRecordHistory) {
                priceHistoryRepository.insert(
                    AssetPriceHistory(
                        id = uuid4().toString(),
                        assetId = assetId,
                        price = price,
                        recordedAt = updatedAt
                    )
                )
            }
        }
        return updateResult
    }
}
