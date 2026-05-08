package es.aviferdev.trackfolio.data.datasource.fixedincome

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.trackfolio.data.database.TrackfolioDatabase
import es.aviferdev.trackfolio.data.database.mapper.toDomain
import es.aviferdev.trackfolio.data.database.mapper.toEntity
import es.aviferdev.trackfolio.domain.model.FixedIncomePosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FixedIncomeLocalDataSourceImpl(
    private val database: TrackfolioDatabase
) : FixedIncomeLocalDataSource {

    private val queries = database.fixedIncomePositionQueries

    override fun getByAccount(accountId: String): Flow<List<FixedIncomePosition>> =
        queries.selectByAccount(accountId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getOpenByAccount(accountId: String): Flow<List<FixedIncomePosition>> =
        queries.selectOpenByAccount(accountId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getById(id: String): Flow<FixedIncomePosition?> =
        queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override fun getNearMaturity(accountId: String, thresholdDate: Long): Flow<List<FixedIncomePosition>> =
        queries.selectNearMaturity(accountId, thresholdDate)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun insert(position: FixedIncomePosition): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                val e = position.toEntity()
                queries.insert(
                    id                = e.id,
                    accountId         = e.accountId,
                    name              = e.name,
                    ticker            = e.ticker,
                    type              = e.type,
                    notes             = e.notes,
                    principal         = e.principal,
                    quantity          = e.quantity,
                    nominalPerUnit    = e.nominalPerUnit,
                    interestRate      = e.interestRate,
                    interestFrequency = e.interestFrequency,
                    startDate         = e.startDate,
                    maturityDate      = e.maturityDate,
                    platformId        = e.platformId,
                    issuerId          = e.issuerId,
                    autoRenew         = e.autoRenew,
                    feeNote           = e.feeNote,
                    createdAt         = e.createdAt
                )
            }
        }

    override suspend fun update(position: FixedIncomePosition): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                val e = position.toEntity()
                queries.update(
                    id                = e.id,
                    name              = e.name,
                    ticker            = e.ticker,
                    type              = e.type,
                    notes             = e.notes,
                    principal         = e.principal,
                    quantity          = e.quantity,
                    nominalPerUnit    = e.nominalPerUnit,
                    interestRate      = e.interestRate,
                    interestFrequency = e.interestFrequency,
                    startDate         = e.startDate,
                    maturityDate      = e.maturityDate,
                    platformId        = e.platformId,
                    issuerId          = e.issuerId,
                    autoRenew         = e.autoRenew,
                    feeNote           = e.feeNote
                )
            }
        }

    override suspend fun close(id: String, closedAt: Long, closeType: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.close(id = id, closedAt = closedAt, closeType = closeType)
            }
        }

    override suspend fun archive(id: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) { queries.archive(id) }
        }
}