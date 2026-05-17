package es.aviferdev.n3to.domain.repository

import es.aviferdev.n3to.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAllAccounts(): Flow<List<Account>>
    fun getAccountById(id: String): Flow<Account?>
    fun getTotalComputedBalance(): Flow<Double>
    suspend fun saveAccount(account: Account): Result<Unit>
    suspend fun updateAccount(account: Account): Result<Unit>
    suspend fun setInitialBalance(accountId: String, amount: Double): Result<Unit>
    suspend fun deleteAccount(accountId: String): Result<Unit>
}
