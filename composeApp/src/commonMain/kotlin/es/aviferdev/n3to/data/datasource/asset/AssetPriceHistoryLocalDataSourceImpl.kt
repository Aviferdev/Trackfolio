package es.aviferdev.n3to.data.datasource.asset

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import es.aviferdev.n3to.data.database.N3toDatabase
import es.aviferdev.n3to.data.database.mapper.toDomain
import es.aviferdev.n3to.domain.model.AssetPriceHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AssetPriceHistoryLocalDataSourceImpl(
    private val database: N3toDatabase
) : AssetPriceHistoryLocalDataSource {

    private val queries = database.assetPriceHistoryQueries

    override fun getByAsset(assetId: String): Flow<List<AssetPriceHistory>> =
        queries.selectByAsset(assetId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getByAssetInRange(
        assetId: String,
        fromDate: Long,
        toDate: Long
    ): Flow<List<AssetPriceHistory>> =
        queries.selectByAssetInRange(assetId, fromDate, toDate)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getByAccount(accountId: String): Flow<List<AssetPriceHistory>> =
        queries.selectByAccount(accountId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun insert(record: AssetPriceHistory): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.insert(
                    id         = record.id,
                    assetId    = record.assetId,
                    price      = record.price,
                    recordedAt = record.recordedAt
                )
            }
        }

    override suspend fun deleteByAsset(assetId: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) { queries.deleteByAsset(assetId) }
        }
}
