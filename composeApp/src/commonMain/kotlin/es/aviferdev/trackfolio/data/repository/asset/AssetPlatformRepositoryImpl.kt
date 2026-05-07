package es.aviferdev.trackfolio.data.repository.asset

import es.aviferdev.trackfolio.data.datasource.asset.AssetPlatformLocalDataSource
import es.aviferdev.trackfolio.domain.model.Platform
import es.aviferdev.trackfolio.domain.repository.AssetPlatformRepository
import kotlinx.coroutines.flow.Flow

class AssetPlatformRepositoryImpl(
    private val dataSource: AssetPlatformLocalDataSource
) : AssetPlatformRepository {

    override fun getPlatformsByAsset(assetId: String): Flow<List<Platform>> =
        dataSource.getPlatformsByAsset(assetId)

    override fun countByAsset(assetId: String): Flow<Long> =
        dataSource.countByAsset(assetId)

    override fun countByPlatform(platformId: String): Flow<Long> =
        dataSource.countByPlatform(platformId)

    override suspend fun link(assetId: String, platformId: String): Result<Unit> =
        dataSource.link(assetId, platformId)

    override suspend fun unlink(assetId: String, platformId: String): Result<Unit> =
        dataSource.unlink(assetId, platformId)

    override suspend fun unlinkAllByAsset(assetId: String): Result<Unit> =
        dataSource.unlinkAllByAsset(assetId)
}
