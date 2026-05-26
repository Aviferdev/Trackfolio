package es.aviferdev.n3to.data.datasource.account

import es.aviferdev.n3to.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountLocalDataSource {
    fun getAllAccounts(): Flow<List<Account>>
    fun getAccountById(id: String): Flow<Account?>
    fun getTotalComputedBalance(): Flow<Double>
    suspend fun insertAccount(account: Account)
    suspend fun updateAccount(account: Account)
    suspend fun updateInitialBalance(accountId: String, amount: Double)
    suspend fun archiveAccount(accountId: String)
    suspend fun deleteAccount(accountId: String)
    fun count(): Flow<Long>
}
