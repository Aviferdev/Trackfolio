package es.aviferdev.trackfolio.data.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import es.aviferdev.trackfolio.data.database.GetAnnualTotals
import es.aviferdev.trackfolio.data.database.GetMonthlyTotals
import es.aviferdev.trackfolio.data.database.TrackfolioDatabase
import es.aviferdev.trackfolio.data.database.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TransactionLocalDataSourceImpl(
    private val database: TrackfolioDatabase
) : TransactionLocalDataSource {

    private val queries = database.transactionQueries

    override fun getAll(): Flow<List<TransactionEntity>> =
        queries.selectAll().asFlow().mapToList(Dispatchers.IO)

    override fun getByAccount(accountId: String): Flow<List<TransactionEntity>> =
        queries.selectByAccount(accountId).asFlow().mapToList(Dispatchers.IO)

    override fun getByMonth(year: String, month: String): Flow<List<TransactionEntity>> =
        queries.selectByMonth(year = year, month = month).asFlow().mapToList(Dispatchers.IO)

    override fun getMonthlyTotals(year: String, month: String): Flow<GetMonthlyTotals> =
        queries.getMonthlyTotals(year = year, month = month).asFlow().mapToOne(Dispatchers.IO)

    override fun getAnnualTotals(year: String): Flow<GetAnnualTotals> =
        queries.getAnnualTotals(year).asFlow().mapToOne(Dispatchers.IO)

    override suspend fun insert(entity: TransactionEntity): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.insert(
                    id = entity.id,
                    accountId = entity.accountId,
                    amount = entity.amount,
                    type = entity.type,
                    categoryId = entity.categoryId,
                    date = entity.date,
                    notes = entity.notes,
                    createdAt = entity.createdAt
                )
            }
        }

    override suspend fun update(entity: TransactionEntity): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.update(
                    accountId = entity.accountId,
                    amount = entity.amount,
                    type = entity.type,
                    categoryId = entity.categoryId,
                    date = entity.date,
                    notes = entity.notes,
                    id = entity.id
                )
            }
        }

    override suspend fun delete(id: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.delete(id)
            }
        }
}
