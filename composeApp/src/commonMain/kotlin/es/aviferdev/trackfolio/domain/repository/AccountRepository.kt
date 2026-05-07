package es.aviferdev.trackfolio.domain.repository

import es.aviferdev.trackfolio.domain.model.Account
import es.aviferdev.trackfolio.domain.model.AccountType
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAllAccounts(): Flow<List<Account>>
    fun getAccountById(id: String): Flow<Account?>
    fun getTotalComputedBalance(): Flow<Double>
    suspend fun saveAccount(account: Account): Result<Unit>
    suspend fun updateAccount(account: Account): Result<Unit>
    suspend fun setInitialBalance(accountId: String, amount: Double): Result<Unit>
    suspend fun updateAccountType(accountId: String, accountType: AccountType): Result<Unit>
    suspend fun deleteAccount(accountId: String): Result<Unit>
}
