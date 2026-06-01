package es.aviferdev.n3to.data.repository.platform

import es.aviferdev.n3to.data.datasource.platform.PlatformLocalDataSource
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.domain.repository.PlatformCategoryRepository
import kotlinx.coroutines.flow.Flow

class PlatformCategoryRepositoryImpl(
    private val platformDataSource: PlatformLocalDataSource
) : PlatformCategoryRepository {

    override fun getByCategory(assetCategoryId: String): Flow<List<Platform>> =
        platformDataSource.getPlatformsByCategory(assetCategoryId)

    override fun getByCategoryIncludingArchived(assetCategoryId: String): Flow<List<Platform>> =
        platformDataSource.getPlatformsByCategoryIncludingArchived(assetCategoryId)

    override fun getCategoriesByPlatform(platformId: String): Flow<List<String>> =
        platformDataSource.getCategoriesByPlatform(platformId)

    override suspend fun link(platformId: String, assetCategoryId: String): Result<Unit> =
        platformDataSource.linkPlatformToCategory(platformId, assetCategoryId)

    override suspend fun unlink(platformId: String, assetCategoryId: String): Result<Unit> =
        platformDataSource.unlinkPlatformFromCategory(platformId, assetCategoryId)

    override suspend fun unlinkAllByCategory(assetCategoryId: String): Result<Unit> =
        platformDataSource.unlinkAllPlatformsByCategory(assetCategoryId)
}
