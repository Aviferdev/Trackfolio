package es.aviferdev.trackfolio.data.repository.asset

import es.aviferdev.trackfolio.data.datasource.asset.AssetPriceHistoryLocalDataSource
import es.aviferdev.trackfolio.domain.model.AssetPriceHistory
import es.aviferdev.trackfolio.domain.repository.AssetPriceHistoryRepository
import kotlinx.coroutines.flow.Flow

class AssetPriceHistoryRepositoryImpl(
    private val dataSource: AssetPriceHistoryLocalDataSource
) : AssetPriceHistoryRepository {

    override fun getByAsset(assetId: String): Flow<List<AssetPriceHistory>> =
        dataSource.getByAsset(assetId)

    override fun getByAssetInRange(assetId: String, fromDate: Long, toDate: Long): Flow<List<AssetPriceHistory>> =
        dataSource.getByAssetInRange(assetId, fromDate, toDate)

    override suspend fun insert(record: AssetPriceHistory): Result<Unit> =
        dataSource.insert(record)

    override suspend fun deleteByAsset(assetId: String): Result<Unit> =
        dataSource.deleteByAsset(assetId)
}
