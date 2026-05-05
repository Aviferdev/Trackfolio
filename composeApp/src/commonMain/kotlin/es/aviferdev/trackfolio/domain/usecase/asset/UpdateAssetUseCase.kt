package es.aviferdev.trackfolio.domain.usecase.asset

import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.repository.AssetRepository

class UpdateAssetUseCase(private val repository: AssetRepository) {
    suspend operator fun invoke(asset: Asset): Result<Unit> =
        repository.updateAsset(asset)
}
