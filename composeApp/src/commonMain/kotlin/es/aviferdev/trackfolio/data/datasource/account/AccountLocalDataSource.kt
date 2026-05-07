package es.aviferdev.trackfolio.data.datasource.account

import es.aviferdev.trackfolio.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountLocalDataSource {
    fun getAllAccounts(): Flow<List<Account>>
    fun getAccountById(id: String): Flow<Account?>
    fun getTotalComputedBalance(): Flow<Double>
    suspend fun insertAccount(account: Account)
    suspend fun updateAccount(account: Account)
    suspend fun updateInitialBalance(accountId: String, amount: Double)
    suspend fun updateAccountType(accountId: String, accountType: String)
    suspend fun deleteAccount(accountId: String)
    fun count(): Flow<Long>
}
