package es.aviferdev.trackfolio.domain.usecase.assettransaction

import es.aviferdev.trackfolio.domain.model.AssetTransaction
import es.aviferdev.trackfolio.domain.repository.AssetTransactionRepository
import kotlinx.coroutines.flow.Flow

class GetTransactionsByAssetUseCase(
    private val repository: AssetTransactionRepository
) {
    operator fun invoke(assetId: String): Flow<List<AssetTransaction>> =
        repository.getByAsset(assetId)
}

class GetTransactionsByAssetDescUseCase(
    private val repository: AssetTransactionRepository
) {
    operator fun invoke(assetId: String): Flow<List<AssetTransaction>> =
        repository.getByAssetDesc(assetId)
}

class GetTransactionsByAccountUseCase(
    private val repository: AssetTransactionRepository
) {
    operator fun invoke(accountId: String): Flow<List<AssetTransaction>> =
        repository.getByAccount(accountId)
}

class SaveAssetTransactionUseCase(
    private val repository: AssetTransactionRepository
) {
    suspend operator fun invoke(tx: AssetTransaction): Result<Unit> = repository.save(tx)
}

class UpdateAssetTransactionUseCase(
    private val repository: AssetTransactionRepository
) {
    suspend operator fun invoke(tx: AssetTransaction): Result<Unit> = repository.update(tx)
}

class DeleteAssetTransactionUseCase(
    private val repository: AssetTransactionRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.delete(id)
}
