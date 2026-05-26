package es.aviferdev.n3to.domain.usecase.portfolio

import es.aviferdev.n3to.domain.repository.AssetRepository
import kotlinx.coroutines.flow.first

class TransferAssetToPortfolioUseCase(
    private val assetRepository: AssetRepository
) {
    suspend operator fun invoke(assetId: String, targetPortfolioId: String?): Result<Unit> {
        val asset = assetRepository.getAssetById(assetId).first()
            ?: return Result.failure(NoSuchElementException("Asset $assetId not found"))
        return assetRepository.updateAsset(asset.copy(portfolioId = targetPortfolioId))
    }
}
