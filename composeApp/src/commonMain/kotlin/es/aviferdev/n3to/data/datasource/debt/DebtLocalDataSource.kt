package es.aviferdev.n3to.data.datasource.debt

import es.aviferdev.n3to.data.database.DebtEntity
import kotlinx.coroutines.flow.Flow

interface DebtLocalDataSource {
    fun getActiveByAccount(accountId: String): Flow<List<DebtEntity>>
    fun getActive(): Flow<List<DebtEntity>>
    fun getAll(): Flow<List<DebtEntity>>
    fun getById(id: String): Flow<DebtEntity?>
    fun getTotalByDirection(direction: String): Flow<Double>
    fun getTotalByDirectionAndAccount(accountId: String, direction: String): Flow<Double>
    suspend fun insert(entity: DebtEntity): Result<Unit>
    suspend fun update(entity: DebtEntity): Result<Unit>
    suspend fun markAsPaid(id: String): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
}
