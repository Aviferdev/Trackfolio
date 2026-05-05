package es.aviferdev.trackfolio.data.repository

import es.aviferdev.trackfolio.data.datasource.AssetLocalDataSource
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow

class AssetRepositoryImpl(
    private val dataSource: AssetLocalDataSource
) : AssetRepository {

    override fun getAssetsByAccount(accountId: String): Flow<List<Asset>> =
        dataSource.getByAccount(accountId)

    override fun getAssetById(id: String): Flow<Asset?> =
        dataSource.getById(id)

    override fun getTotalInvestedByAccount(accountId: String): Flow<Double> =
        dataSource.getTotalInvestedByAccount(accountId)

    override suspend fun saveAsset(asset: Asset): Result<Unit> =
        dataSource.insert(asset)

    override suspend fun updateAsset(asset: Asset): Result<Unit> =
        dataSource.update(asset)

    override suspend fun updateCurrentPrice(id: String, price: Double, updatedAt: Long): Result<Unit> =
        dataSource.updateCurrentPrice(id, price, updatedAt)

    override suspend fun deleteAsset(id: String): Result<Unit> =
        dataSource.delete(id)
}
