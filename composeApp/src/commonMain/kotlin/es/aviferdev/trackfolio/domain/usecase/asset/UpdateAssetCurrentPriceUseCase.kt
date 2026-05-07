package es.aviferdev.trackfolio.domain.usecase.asset

import com.benasher44.uuid.uuid4
import es.aviferdev.trackfolio.domain.model.AssetPriceHistory
import es.aviferdev.trackfolio.domain.repository.AssetPriceHistoryRepository
import es.aviferdev.trackfolio.domain.repository.AssetRepository

/**
 * Actualiza el precio actual y la fecha de última actualización de un activo.
 * Además, registra una entrada en el histórico de precios para poder trazar
 * la evolución del portfolio a lo largo del tiempo.
 */
class UpdateAssetCurrentPriceUseCase(
    private val repository: AssetRepository,
    private val priceHistoryRepository: AssetPriceHistoryRepository
) {
    suspend operator fun invoke(
        assetId: String,
        price: Double,
        updatedAt: Long
    ): Result<Unit> {
        val updateResult = repository.updateCurrentPrice(assetId, price, updatedAt)
        if (updateResult.isSuccess) {
            priceHistoryRepository.insert(
                AssetPriceHistory(
                    id         = uuid4().toString(),
                    assetId    = assetId,
                    price      = price,
                    recordedAt = updatedAt
                )
            )
        }
        return updateResult
    }
}
