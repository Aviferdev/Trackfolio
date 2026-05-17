package es.aviferdev.n3to.data.datasource.asset

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.n3to.data.database.N3toDatabase
import es.aviferdev.n3to.data.database.mapper.toDomain
import es.aviferdev.n3to.data.database.mapper.toEntity
import es.aviferdev.n3to.domain.model.AssetTransaction
import es.aviferdev.n3to.domain.model.MonthlyInvestment
import es.aviferdev.n3to.domain.model.MonthlyNetInvestment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AssetTransactionLocalDataSourceImpl(
    private val database: N3toDatabase
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

    override fun getOldestDate(accountId: String): Flow<Long?> =
        queries.getOldestDateByAccount(accountId).asFlow().mapToOneOrNull(Dispatchers.IO)
            .map { it?.oldestDate }

    override fun getMonthlyInvestmentsByYear(accountId: String, year: String): Flow<List<MonthlyInvestment>> =
        queries.getMonthlyInvestmentsByYear(accountId, year)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    MonthlyInvestment(
                        year  = year,
                        month = row.month ?: "01",
                        amount = row.totalInvested ?: 0.0
                    )
                }
            }

    override fun getMonthlyNetInvestmentsByYear(
        accountId: String,
        year: String
    ): Flow<List<MonthlyNetInvestment>> =
        queries.getMonthlyNetInvestmentsByYear(accountId, year)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    MonthlyNetInvestment(
                        year = year,
                        month = row.month ?: "01",
                        netAmount = row.netInvested ?: 0.0
                    )
                }
            }

    override fun getMonthlyNetInvestmentByMonth(
        accountId: String,
        yearMonth: String
    ): Flow<MonthlyNetInvestment?> =
        queries.getMonthlyNetInvestmentByMonth(accountId, yearMonth)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { row ->
                row?.let {
                    MonthlyNetInvestment(
                        year = yearMonth.substringBefore("-"),
                        month = yearMonth.substringAfter("-"),
                        netAmount = it.netInvested ?: 0.0
                    )
                }
            }
}
