package es.aviferdev.n3to.domain.usecase.asset

import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow

class GetAssetsByAccountUseCase(private val repository: AssetRepository) {
    operator fun invoke(accountId: String): Flow<List<Asset>> =
        repository.getAssetsByAccount(accountId)
}
