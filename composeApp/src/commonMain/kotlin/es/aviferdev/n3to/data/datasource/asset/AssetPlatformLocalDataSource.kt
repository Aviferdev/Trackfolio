package es.aviferdev.n3to.data.datasource.asset

import es.aviferdev.n3to.domain.model.Platform
import kotlinx.coroutines.flow.Flow

interface AssetPlatformLocalDataSource {
    fun getPlatformsByAsset(assetId: String): Flow<List<Platform>>
    fun countByAsset(assetId: String): Flow<Long>
    fun countByPlatform(platformId: String): Flow<Long>
    suspend fun link(assetId: String, platformId: String): Result<Unit>
    suspend fun unlink(assetId: String, platformId: String): Result<Unit>
    suspend fun unlinkAllByAsset(assetId: String): Result<Unit>
}
