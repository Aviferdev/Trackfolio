package es.aviferdev.trackfolio.data.datasource.debt

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.trackfolio.data.database.DebtEntity
import es.aviferdev.trackfolio.data.database.TrackfolioDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DebtLocalDataSourceImpl(
    private val database: TrackfolioDatabase
) : DebtLocalDataSource {

    private val queries = database.debtQueries

    override fun getActiveByAccount(accountId: String): Flow<List<DebtEntity>> =
        queries.selectActiveByAccount(accountId).asFlow().mapToList(Dispatchers.IO)

    override fun getActive(): Flow<List<DebtEntity>> =
        queries.selectActive().asFlow().mapToList(Dispatchers.IO)

    override fun getAll(): Flow<List<DebtEntity>> =
        queries.selectAll().asFlow().mapToList(Dispatchers.IO)

    override fun getById(id: String): Flow<DebtEntity?> =
        queries.selectById(id).asFlow().mapToOneOrNull(Dispatchers.IO)

    override fun getTotalByDirection(direction: String): Flow<Double> =
        queries.getTotalByDirection(direction).asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it ?: 0.0 }

    override fun getTotalByDirectionAndAccount(accountId: String, direction: String): Flow<Double> =
        queries.getTotalByDirectionAndAccount(accountId, direction).asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it ?: 0.0 }

    override suspend fun insert(entity: DebtEntity): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.insert(
                    id         = entity.id,
                    accountId  = entity.accountId,
                    personName = entity.personName,
                    amount     = entity.amount,
                    direction  = entity.direction,
                    date       = entity.date,
                    isPaid     = entity.isPaid,
                    notes      = entity.notes,
                    createdAt  = entity.createdAt
                )
            }
        }

    override suspend fun update(entity: DebtEntity): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.update(
                    personName = entity.personName,
                    amount     = entity.amount,
                    direction  = entity.direction,
                    date       = entity.date,
                    notes      = entity.notes,
                    id         = entity.id
                )
            }
        }

    override suspend fun markAsPaid(id: String): Result<Unit> =
        runCatching { withContext(Dispatchers.IO) { queries.markAsPaid(id) } }

    override suspend fun delete(id: String): Result<Unit> =
        runCatching { withContext(Dispatchers.IO) { queries.delete(id) } }
}
