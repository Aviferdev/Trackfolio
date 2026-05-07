package es.aviferdev.trackfolio.domain.repository

import es.aviferdev.trackfolio.domain.model.Asset
import kotlinx.coroutines.flow.Flow

interface AssetRepository {
    fun getAssetsByAccount(accountId: String): Flow<List<Asset>>
    fun getAssetById(id: String): Flow<Asset?>
    suspend fun saveAsset(asset: Asset): Result<Unit>
    suspend fun updateAsset(asset: Asset): Result<Unit>
    suspend fun updateCurrentPrice(id: String, price: Double, updatedAt: Long): Result<Unit>
    suspend fun deleteAsset(id: String): Result<Unit>
    fun getOutdatedAssets(accountId: String, thresholdDate: Long): Flow<List<Asset>>
}
