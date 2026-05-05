package es.aviferdev.trackfolio.domain.usecase.asset

import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow

class GetAssetsByAccountUseCase(private val repository: AssetRepository) {
    operator fun invoke(accountId: String): Flow<List<Asset>> =
        repository.getAssetsByAccount(accountId)
}
