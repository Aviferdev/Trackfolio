package es.aviferdev.trackfolio.data.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.trackfolio.data.database.TrackfolioDatabase
import es.aviferdev.trackfolio.data.database.mapper.toDomain
import es.aviferdev.trackfolio.data.database.mapper.toEntity
import es.aviferdev.trackfolio.domain.model.AssetTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AssetTransactionLocalDataSourceImpl(
    private val database: TrackfolioDatabase
) : AssetTransactionLocalDataSource {

    private val queries = database.assetTransactionQueries

    override fun getByAsset(assetId: String): Flow<List<AssetTransaction>> =
        queries.selectByAsset(assetId).asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getByAssetDesc(assetId: String): Flow<List<AssetTransaction>> =
        queries.selectByAssetDesc(assetId).asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getByAccount(accountId: String): Flow<List<AssetTransaction>> =
        queries.selectByAccount(accountId).asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getById(id: String): Flow<AssetTransaction?> =
        queries.selectById(id).asFlow().mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override fun countByPlatform(platformId: String): Flow<Long> =
        queries.countByPlatform(platformId).asFlow().mapToOne(Dispatchers.IO)

    override suspend fun insert(tx: AssetTransaction): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val e = tx.toEntity()
            queries.insert(
                id           = e.id,
                assetId      = e.assetId,
                type         = e.type,
                quantity     = e.quantity,
                pricePerUnit = e.pricePerUnit,
                date         = e.date,
                platformId   = e.platformId,
                feeNote      = e.feeNote,
                notes        = e.notes,
                createdAt    = e.createdAt
            )
        }
    }

    override suspend fun update(tx: AssetTransaction): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val e = tx.toEntity()
            queries.update(
                type         = e.type,
                quantity     = e.quantity,
                pricePerUnit = e.pricePerUnit,
                date         = e.date,
                platformId   = e.platformId,
                feeNote      = e.feeNote,
                notes        = e.notes,
                id           = e.id
            )
        }
    }

    override suspend fun delete(id: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) { queries.delete(id) }
    }
}
