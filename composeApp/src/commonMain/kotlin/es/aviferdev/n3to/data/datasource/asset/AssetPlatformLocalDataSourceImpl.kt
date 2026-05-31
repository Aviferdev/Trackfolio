package es.aviferdev.n3to.data.datasource.asset

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import es.aviferdev.n3to.data.database.N3toDatabase
import es.aviferdev.n3to.data.database.mapper.toDomain
import es.aviferdev.n3to.domain.model.Platform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AssetPlatformLocalDataSourceImpl(
    private val database: N3toDatabase
) : AssetPlatformLocalDataSource {

    private val queries = database.assetPlatformQueries

    override fun getPlatformsByAsset(assetId: String): Flow<List<Platform>> =
        queries.selectByAsset(assetId).asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun countByAsset(assetId: String): Flow<Long> =
        queries.countByAsset(assetId).asFlow().mapToOne(Dispatchers.IO)

    override fun countByPlatform(platformId: String): Flow<Long> =
        queries.countByPlatform(platformId).asFlow().mapToOne(Dispatchers.IO)

    override suspend fun link(assetId: String, platformId: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.insert(assetId, platformId)
        }
    }

    override suspend fun unlink(assetId: String, platformId: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) { queries.delete(assetId, platformId) }
    }

    override suspend fun unlinkAllByAsset(assetId: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) { queries.deleteAllByAsset(assetId) }
    }
}
