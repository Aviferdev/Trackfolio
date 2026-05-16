package es.aviferdev.n3to.data.repository.asset

import es.aviferdev.n3to.data.datasource.asset.AssetPlatformLocalDataSource
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.domain.repository.AssetPlatformRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class AssetPlatformRepositoryImpl(
    private val dataSource: AssetPlatformLocalDataSource
) : AssetPlatformRepository {

    override fun getPlatformsByAsset(assetId: String): Flow<List<Platform>> =
        dataSource.getPlatformsByAsset(assetId)

    override fun getPlatformsByAssets(assetIds: List<String>): Flow<Map<String, List<Platform>>> {
        if (assetIds.isEmpty()) return flowOf(emptyMap())

        val flows = assetIds.map { assetId ->
            getPlatformsByAsset(assetId).map { platforms ->
                assetId to platforms
            }
        }

        return combine(flows) { pairs ->
            pairs.toMap()
        }
    }

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
