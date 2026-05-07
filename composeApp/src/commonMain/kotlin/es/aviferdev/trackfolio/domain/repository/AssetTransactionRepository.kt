package es.aviferdev.trackfolio.domain.repository

import es.aviferdev.trackfolio.domain.model.AssetTransaction
import kotlinx.coroutines.flow.Flow

interface AssetTransactionRepository {
    fun getByAsset(assetId: String): Flow<List<AssetTransaction>>
    fun getByAssetDesc(assetId: String): Flow<List<AssetTransaction>>
    fun getByAccount(accountId: String): Flow<List<AssetTransaction>>
    fun getById(id: String): Flow<AssetTransaction?>
    fun countByPlatform(platformId: String): Flow<Long>

    suspend fun save(tx: AssetTransaction): Result<Unit>
    suspend fun update(tx: AssetTransaction): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
    fun getOldestDate(accountId: String): Flow<Long?>
}
