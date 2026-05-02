package es.aviferdev.trackfolio.data.datasource

import es.aviferdev.trackfolio.data.database.AccountEntity
import kotlinx.coroutines.flow.Flow

interface AccountLocalDataSource {
    fun getAll(): Flow<List<AccountEntity>>
    fun getById(id: String): Flow<AccountEntity?>
    fun getTotalBalance(): Flow<Double>
    suspend fun insert(entity: AccountEntity): Result<Unit>
    suspend fun update(entity: AccountEntity): Result<Unit>
    suspend fun updateBalance(id: String, balance: Double): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
}
