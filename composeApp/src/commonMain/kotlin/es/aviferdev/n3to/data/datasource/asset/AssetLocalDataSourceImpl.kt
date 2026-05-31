package es.aviferdev.n3to.data.datasource.asset

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.n3to.data.database.N3toDatabase
import es.aviferdev.n3to.data.database.mapper.toDomain
import es.aviferdev.n3to.data.database.mapper.toEntity
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.PriceSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AssetLocalDataSourceImpl(
    private val database: N3toDatabase
) : AssetLocalDataSource {

    private val queries = database.assetQueries

    override fun getByAccount(accountId: String): Flow<List<Asset>> =
        queries.selectByAccount(accountId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getByPortfolio(portfolioId: String): Flow<List<Asset>> =
        queries.selectByPortfolio(portfolioId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getByAccountWithoutPortfolio(accountId: String): Flow<List<Asset>> =
        queries.selectByAccountWithoutPortfolio(accountId)
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
                    id = e.id,
                    accountId = e.accountId,
                    portfolioId = e.portfolioId,
                    ticker = e.ticker,
                    name = e.name,
                    notes = e.notes,
                    createdAt = e.createdAt,
                    assetCategoryId = e.assetCategoryId,
                    currentPrice = e.currentPrice,
                    currentPriceUpdatedAt = e.currentPriceUpdatedAt,
                    archived = e.archived,
                    maturityDate = e.maturityDate,
                    isin = e.isin,
                    priceSource = e.priceSource,
                    isinValidatedAt = e.isinValidatedAt,
                    isinValidationError = e.isinValidationError,
                    fixedIncomePercent = e.fixedIncomePercent
                )
            }
        }

    override suspend fun update(asset: Asset): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                val e = asset.toEntity()
                queries.update(
                    ticker = e.ticker,
                    name = e.name,
                    notes = e.notes,
                    assetCategoryId = e.assetCategoryId,
                    portfolioId = e.portfolioId,
                    currentPrice = e.currentPrice,
                    currentPriceUpdatedAt = e.currentPriceUpdatedAt,
                    maturityDate = e.maturityDate,
                    isin = e.isin,
                    priceSource = e.priceSource,
                    isinValidatedAt = e.isinValidatedAt,
                    isinValidationError = e.isinValidationError,
                    fixedIncomePercent = e.fixedIncomePercent,
                    id = e.id
                )
            }
        }

    override suspend fun updateCurrentPrice(
        id: String,
        price: Double,
        updatedAt: Long
    ): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.updateCurrentPrice(
                    currentPrice = price,
                    currentPriceUpdatedAt = updatedAt,
                    id = id
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

    // ── Nuevos métodos para auto-precio e ISIN ──

    override suspend fun updateIsin(id: String, isin: String?, validatedAt: Long?): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.updateIsin(isin = isin, validatedAt = validatedAt, id = id)
            }
        }

    override suspend fun updatePriceSource(id: String, priceSource: PriceSource): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.updatePriceSource(priceSource = priceSource.name, id = id)
            }
        }

    override suspend fun markIsinValidationError(id: String, error: String?): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.markIsinValidationError(error = error, id = id)
            }
        }

    override fun getQuotableByAccount(accountId: String): Flow<List<Asset>> =
        queries.selectQuotableByAccount(accountId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { entity -> entity.toDomain() } }

    override fun getAssetsWithBrokenIsin(accountId: String): Flow<List<Asset>> =
        queries.selectAssetsWithBrokenIsin(accountId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { entity -> entity.toDomain() } }
}
