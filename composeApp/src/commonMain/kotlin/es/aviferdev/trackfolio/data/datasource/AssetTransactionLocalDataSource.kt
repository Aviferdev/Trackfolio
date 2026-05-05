package es.aviferdev.trackfolio.data.datasource

import es.aviferdev.trackfolio.domain.model.AssetTransaction
import kotlinx.coroutines.flow.Flow

interface AssetTransactionLocalDataSource {
    fun getByAsset(assetId: String): Flow<List<AssetTransaction>>
    fun getByAssetDesc(assetId: String): Flow<List<AssetTransaction>>
    fun getByAccount(accountId: String): Flow<List<AssetTransaction>>
    fun getById(id: String): Flow<AssetTransaction?>
    fun countByPlatform(platformId: String): Flow<Long>

    suspend fun insert(tx: AssetTransaction): Result<Unit>
    suspend fun update(tx: AssetTransaction): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
}
