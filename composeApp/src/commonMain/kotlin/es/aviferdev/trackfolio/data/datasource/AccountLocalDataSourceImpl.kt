package es.aviferdev.trackfolio.data.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.trackfolio.data.database.AccountEntity
import es.aviferdev.trackfolio.data.database.TrackfolioDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AccountLocalDataSourceImpl(
    private val database: TrackfolioDatabase
) : AccountLocalDataSource {

    private val queries = database.accountQueries

    override fun getAll(): Flow<List<AccountEntity>> =
        queries.selectAll().asFlow().mapToList(Dispatchers.IO)

    override fun getById(id: String): Flow<AccountEntity?> =
        queries.selectById(id).asFlow().mapToOneOrNull(Dispatchers.IO)

    override fun getTotalBalance(): Flow<Double> =
        queries.getTotalBalance().asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it ?: 0.0 }

    override suspend fun insert(entity: AccountEntity): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.insert(
                    id = entity.id,
                    name = entity.name,
                    type = entity.type,
                    currency = entity.currency,
                    balance = entity.balance,
                    createdAt = entity.createdAt
                )
            }
        }

    override suspend fun update(entity: AccountEntity): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.update(
                    name = entity.name,
                    type = entity.type,
                    currency = entity.currency,
                    balance = entity.balance,
                    id = entity.id
                )
            }
        }

    override suspend fun updateBalance(id: String, balance: Double): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.updateBalance(balance = balance, id = id)
            }
        }

    override suspend fun delete(id: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.delete(id)
            }
        }
}
