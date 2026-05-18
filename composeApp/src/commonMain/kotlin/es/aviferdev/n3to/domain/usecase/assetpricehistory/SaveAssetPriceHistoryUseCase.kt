package es.aviferdev.n3to.domain.usecase.assetpricehistory

import com.benasher44.uuid.uuid4
import es.aviferdev.n3to.domain.model.AssetPriceHistory
import es.aviferdev.n3to.domain.repository.AssetPriceHistoryRepository

class SaveAssetPriceHistoryUseCase(
    private val repository: AssetPriceHistoryRepository
) {
    suspend operator fun invoke(assetId: String, price: Double, recordedAt: Long): Result<Unit> =
        repository.insert(
            AssetPriceHistory(
                id = uuid4().toString(),
                assetId = assetId,
                price = price,
                recordedAt = recordedAt
            )
        )
}
