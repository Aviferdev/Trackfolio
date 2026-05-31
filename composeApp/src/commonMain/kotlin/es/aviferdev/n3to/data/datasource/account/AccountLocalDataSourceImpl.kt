package es.aviferdev.n3to.data.datasource.account

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.n3to.data.database.N3toDatabase
import es.aviferdev.n3to.data.database.mapper.toDomain
import es.aviferdev.n3to.domain.model.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AccountLocalDataSourceImpl(
    private val database: N3toDatabase
) : AccountLocalDataSource {

    private val queries = database.accountQueries

    override fun getAllAccounts(): Flow<List<Account>> =
        queries.getAllComputedBalances()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getAccountById(id: String): Flow<Account?> =
        queries.getComputedBalance(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override fun getTotalComputedBalance(): Flow<Double> =
        queries.getTotalComputedBalance()
            .asFlow()
            .mapToOne(Dispatchers.IO)

    override fun count(): Flow<Long> =
        queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.size.toLong() }

    override suspend fun insertAccount(account: Account) {
        withContext(Dispatchers.IO) {
            queries.insert(
                id = account.id,
                name = account.name,
                initialBalance = account.initialBalance,
                createdAt = account.createdAt,
                currency = account.currency
            )
        }
    }

    override suspend fun updateAccount(account: Account) {
        withContext(Dispatchers.IO) {
            queries.update(
                name = account.name,
                id = account.id
            )
        }
    }

    override suspend fun updateInitialBalance(accountId: String, amount: Double) {
        withContext(Dispatchers.IO) {
            queries.updateInitialBalance(initialBalance = amount, id = accountId)
        }
    }

    override suspend fun archiveAccount(accountId: String) {
        withContext(Dispatchers.IO) {
            queries.archive(accountId)
        }
    }

    override suspend fun deleteAccount(accountId: String) {
        withContext(Dispatchers.IO) {
            queries.delete(accountId)
        }
    }
}
