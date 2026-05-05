package es.aviferdev.trackfolio.data.repository

import es.aviferdev.trackfolio.data.datasource.AssetTransactionLocalDataSource
import es.aviferdev.trackfolio.domain.model.AssetTransaction
import es.aviferdev.trackfolio.domain.repository.AssetTransactionRepository
import kotlinx.coroutines.flow.Flow

class AssetTransactionRepositoryImpl(
    private val dataSource: AssetTransactionLocalDataSource
) : AssetTransactionRepository {

    override fun getByAsset(assetId: String): Flow<List<AssetTransaction>> =
        dataSource.getByAsset(assetId)

    override fun getByAssetDesc(assetId: String): Flow<List<AssetTransaction>> =
        dataSource.getByAssetDesc(assetId)

    override fun getByAccount(accountId: String): Flow<List<AssetTransaction>> =
        dataSource.getByAccount(accountId)

    override fun getById(id: String): Flow<AssetTransaction?> =
        dataSource.getById(id)

    override fun countByPlatform(platformId: String): Flow<Long> =
        dataSource.countByPlatform(platformId)

    override suspend fun save(tx: AssetTransaction): Result<Unit> = dataSource.insert(tx)
    override suspend fun update(tx: AssetTransaction): Result<Unit> = dataSource.update(tx)
    override suspend fun delete(id: String): Result<Unit> = dataSource.delete(id)
}
