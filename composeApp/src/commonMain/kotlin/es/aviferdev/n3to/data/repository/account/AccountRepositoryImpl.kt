package es.aviferdev.n3to.data.repository.account

import es.aviferdev.n3to.data.datasource.account.AccountLocalDataSource
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow

class AccountRepositoryImpl(
    private val dataSource: AccountLocalDataSource
) : AccountRepository {

    override fun getAllAccounts(): Flow<List<Account>> =
        dataSource.getAllAccounts()

    override fun getAccountById(id: String): Flow<Account?> =
        dataSource.getAccountById(id)

    override fun getTotalComputedBalance(): Flow<Double> =
        dataSource.getTotalComputedBalance()

    override suspend fun saveAccount(account: Account): Result<Unit> =
        runCatching { dataSource.insertAccount(account) }

    override suspend fun updateAccount(account: Account): Result<Unit> =
        runCatching { dataSource.updateAccount(account) }

    override suspend fun setInitialBalance(accountId: String, amount: Double): Result<Unit> =
        runCatching { dataSource.updateInitialBalance(accountId, amount) }

    override suspend fun deleteAccount(accountId: String): Result<Unit> =
        runCatching { dataSource.deleteAccount(accountId) }
}
