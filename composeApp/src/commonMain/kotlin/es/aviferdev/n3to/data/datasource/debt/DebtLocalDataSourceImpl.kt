package es.aviferdev.n3to.data.datasource.debt

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.n3to.data.database.N3toDatabase
import es.aviferdev.n3to.data.database.mapper.toDomain
import es.aviferdev.n3to.data.database.mapper.toEntity
import es.aviferdev.n3to.domain.model.Debt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DebtLocalDataSourceImpl(
    private val database: N3toDatabase
) : DebtLocalDataSource {

    private val queries = database.debtQueries

    override fun getActiveByAccount(accountId: String): Flow<List<Debt>> =
        queries.selectActiveByAccount(accountId).asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getActive(): Flow<List<Debt>> =
        queries.selectActive().asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getAll(): Flow<List<Debt>> =
        queries.selectAll().asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getByAccount(accountId: String): Flow<List<Debt>> =
        queries.selectByAccount(accountId).asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getById(id: String): Flow<Debt?> =
        queries.selectById(id).asFlow().mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override fun getTotalByDirection(direction: String): Flow<Double> =
        queries.getTotalByDirection(direction).asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it ?: 0.0 }

    override fun getTotalByDirectionAndAccount(accountId: String, direction: String): Flow<Double> =
        queries.getTotalByDirectionAndAccount(accountId, direction).asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it ?: 0.0 }

    override suspend fun insert(entity: Debt): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                val e = entity.toEntity()
                queries.insert(
                    id = e.id,
                    accountId = e.accountId,
                    personName = e.personName,
                    amount = e.amount,
                    direction = e.direction,
                    date = e.date,
                    isPaid = e.isPaid,
                    notes = e.notes,
                    createdAt = e.createdAt
                )
            }
        }

    override suspend fun update(entity: Debt): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                val e = entity.toEntity()
                queries.update(
                    personName = e.personName,
                    amount = e.amount,
                    direction = e.direction,
                    date = e.date,
                    notes = e.notes,
                    id = e.id
                )
            }
        }

    override suspend fun markAsPaid(id: String): Result<Unit> =
        runCatching { withContext(Dispatchers.IO) { queries.markAsPaid(id) } }

    override suspend fun archive(id: String): Result<Unit> =
        runCatching { withContext(Dispatchers.IO) { queries.archive(id) } }

    override suspend fun unarchive(id: String): Result<Unit> =
        runCatching { withContext(Dispatchers.IO) { queries.unarchive(id) } }

    override suspend fun delete(id: String): Result<Unit> =
        runCatching { withContext(Dispatchers.IO) { queries.delete(id) } }
}
