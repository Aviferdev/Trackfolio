package es.aviferdev.trackfolio.data.datasource

import es.aviferdev.trackfolio.data.database.DebtEntity
import kotlinx.coroutines.flow.Flow

interface DebtLocalDataSource {
    fun getActive(): Flow<List<DebtEntity>>
    fun getAll(): Flow<List<DebtEntity>>
    fun getById(id: String): Flow<DebtEntity?>
    fun getTotalByDirection(direction: String): Flow<Double>
    suspend fun insert(entity: DebtEntity): Result<Unit>
    suspend fun update(entity: DebtEntity): Result<Unit>
    suspend fun markAsPaid(id: String): Result<Unit>
}
