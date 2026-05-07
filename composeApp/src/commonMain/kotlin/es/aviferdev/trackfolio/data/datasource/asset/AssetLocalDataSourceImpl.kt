package es.aviferdev.trackfolio.data.datasource.asset

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.trackfolio.data.database.TrackfolioDatabase
import es.aviferdev.trackfolio.data.database.mapper.toDomain
import es.aviferdev.trackfolio.data.database.mapper.toEntity
import es.aviferdev.trackfolio.domain.model.Asset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AssetLocalDataSourceImpl(
    private val database: TrackfolioDatabase
) : AssetLocalDataSource {

    private val queries = database.assetQueries

    override fun getByAccount(accountId: String): Flow<List<Asset>> =
        queries.selectByAccount(accountId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getAllByAccountIncludingArchived(accountId: String): Flow<List<Asset>> =
        queries.selectAllByAccountIncludingArchived(accountId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getById(id: String): Flow<Asset?> =
        queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override suspend fun insert(asset: Asset): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                val e = asset.toEntity()
                queries.insert(
                    id                    = e.id,
                    accountId             = e.accountId,
                    ticker                = e.ticker,
                    name                  = e.name,
                    notes                 = e.notes,
                    createdAt             = e.createdAt,
                    assetCategoryId       = e.assetCategoryId,
                    currentPrice          = e.currentPrice,
                    currentPriceUpdatedAt = e.currentPriceUpdatedAt,
                    maturityDate          = e.maturityDate
                )
            }
        }

    override suspend fun update(asset: Asset): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                val e = asset.toEntity()
                queries.update(
                    ticker                = e.ticker,
                    name                  = e.name,
                    notes                 = e.notes,
                    assetCategoryId       = e.assetCategoryId,
                    currentPrice          = e.currentPrice,
                    currentPriceUpdatedAt = e.currentPriceUpdatedAt,
                    maturityDate          = e.maturityDate,
                    id                    = e.id
                )
            }
        }

    override suspend fun updateCurrentPrice(id: String, price: Double, updatedAt: Long): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.updateCurrentPrice(
                    currentPrice          = price,
                    currentPriceUpdatedAt = updatedAt,
                    id                    = id
                )
            }
        }

    override suspend fun archive(id: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) { queries.archive(id) }
        }

    override suspend fun unarchive(id: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) { queries.unarchive(id) }
        }

    override suspend fun delete(id: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) { queries.delete(id) }
        }

    override fun getOutdatedByAccount(accountId: String, thresholdDate: Long): Flow<List<Asset>> =
        queries.selectOutdatedByAccount(accountId, thresholdDate)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }
}
