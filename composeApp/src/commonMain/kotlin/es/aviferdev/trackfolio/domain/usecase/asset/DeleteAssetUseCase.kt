package es.aviferdev.trackfolio.domain.usecase.asset

import es.aviferdev.trackfolio.domain.repository.AssetRepository

class DeleteAssetUseCase(private val repository: AssetRepository) {
    suspend operator fun invoke(id: String): Result<Unit> =
        repository.deleteAsset(id)
}
