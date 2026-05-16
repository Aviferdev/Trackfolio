package es.aviferdev.n3to.data.datasource.asset

import es.aviferdev.n3to.domain.model.AssetPriceHistory
import kotlinx.coroutines.flow.Flow

interface AssetPriceHistoryLocalDataSource {
    fun getByAsset(assetId: String): Flow<List<AssetPriceHistory>>
    fun getByAssetInRange(assetId: String, fromDate: Long, toDate: Long): Flow<List<AssetPriceHistory>>
    fun getByAccount(accountId: String): Flow<List<AssetPriceHistory>>
    suspend fun insert(record: AssetPriceHistory): Result<Unit>
    suspend fun deleteByAsset(assetId: String): Result<Unit>
}
