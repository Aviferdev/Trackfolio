package es.aviferdev.n3to.data.datasource.platform

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import es.aviferdev.n3to.data.database.N3toDatabase
import es.aviferdev.n3to.data.database.mapper.toDomain
import es.aviferdev.n3to.domain.model.Platform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PlatformCategoryLocalDataSourceImpl(
    private val database: N3toDatabase
) : PlatformCategoryLocalDataSource {

    private val queries = database.platformCategoryQueries

    override fun getByCategory(assetCategoryId: String): Flow<List<Platform>> =
        queries.selectByCategory(assetCategoryId).asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getByCategoryIncludingArchived(assetCategoryId: String): Flow<List<Platform>> =
        queries.selectByCategoryIncludingArchived(assetCategoryId).asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getCategoriesByPlatform(platformId: String): Flow<List<String>> =
        queries.selectCategoriesByPlatform(platformId).asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it } }

    override suspend fun link(platformId: String, assetCategoryId: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.link(platformId, assetCategoryId)
            }
        }

    override suspend fun unlink(platformId: String, assetCategoryId: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.unlink(platformId, assetCategoryId)
            }
        }

    override suspend fun unlinkAllByCategory(assetCategoryId: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.unlinkAllByCategory(assetCategoryId)
        }
    }
}
