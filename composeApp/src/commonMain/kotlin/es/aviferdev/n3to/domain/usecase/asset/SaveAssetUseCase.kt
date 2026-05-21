package es.aviferdev.n3to.domain.usecase.asset

import com.benasher44.uuid.uuid4
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.AssetPriceHistory
import es.aviferdev.n3to.domain.repository.AssetPriceHistoryRepository
import es.aviferdev.n3to.domain.repository.AssetRepository

/**
 * Guarda un nuevo activo y, si tiene precio asignado, registra ese precio
 * como histórico en la fecha de creación para que la gráfica de evolución
 * del portfolio pueda trazar correctamente el valor desde el primer día.
 */
class SaveAssetUseCase(
    private val repository: AssetRepository,
    private val priceHistoryRepository: AssetPriceHistoryRepository
) {
    suspend operator fun invoke(asset: Asset): Result<Unit> {
        val result = repository.saveAsset(asset)
        if (result.isSuccess && asset.currentPrice != null) {
            priceHistoryRepository.insert(
                AssetPriceHistory(
                    id = uuid4().toString(),
                    assetId = asset.id,
                    price = asset.currentPrice,
                    recordedAt = asset.currentPriceUpdatedAt ?: asset.createdAt
                )
            )
        }
        return result
    }
}
