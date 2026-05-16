package es.aviferdev.n3to.data.repository.platform

import es.aviferdev.n3to.data.datasource.platform.PlatformCategoryLocalDataSource
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.domain.repository.PlatformCategoryRepository
import kotlinx.coroutines.flow.Flow

class PlatformCategoryRepositoryImpl(
    private val dataSource: PlatformCategoryLocalDataSource
) : PlatformCategoryRepository {

    override fun getByCategory(assetCategoryId: String): Flow<List<Platform>> =
        dataSource.getByCategory(assetCategoryId)

    override fun getByCategoryIncludingArchived(assetCategoryId: String): Flow<List<Platform>> =
        dataSource.getByCategoryIncludingArchived(assetCategoryId)

    override fun getCategoriesByPlatform(platformId: String): Flow<List<String>> =
        dataSource.getCategoriesByPlatform(platformId)

    override suspend fun link(platformId: String, assetCategoryId: String): Result<Unit> =
        dataSource.link(platformId, assetCategoryId)

    override suspend fun unlink(platformId: String, assetCategoryId: String): Result<Unit> =
        dataSource.unlink(platformId, assetCategoryId)

    override suspend fun unlinkAllByCategory(assetCategoryId: String): Result<Unit> =
        dataSource.unlinkAllByCategory(assetCategoryId)
}
