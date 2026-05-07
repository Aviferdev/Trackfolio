package es.aviferdev.trackfolio.domain.repository

import es.aviferdev.trackfolio.domain.model.AssetPriceHistory
import kotlinx.coroutines.flow.Flow

interface AssetPriceHistoryRepository {
    fun getByAsset(assetId: String): Flow<List<AssetPriceHistory>>
    fun getByAssetInRange(assetId: String, fromDate: Long, toDate: Long): Flow<List<AssetPriceHistory>>
    suspend fun insert(record: AssetPriceHistory): Result<Unit>
    suspend fun deleteByAsset(assetId: String): Result<Unit>
}
