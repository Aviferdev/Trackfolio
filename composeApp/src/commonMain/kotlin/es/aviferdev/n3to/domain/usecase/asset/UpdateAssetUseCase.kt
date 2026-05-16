package es.aviferdev.n3to.domain.usecase.asset

import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.repository.AssetRepository

class UpdateAssetUseCase(private val repository: AssetRepository) {
    suspend operator fun invoke(asset: Asset): Result<Unit> =
        repository.updateAsset(asset)
}
