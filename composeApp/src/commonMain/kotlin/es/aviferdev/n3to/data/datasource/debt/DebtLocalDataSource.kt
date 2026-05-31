package es.aviferdev.n3to.data.datasource.debt

import es.aviferdev.n3to.domain.model.Debt
import kotlinx.coroutines.flow.Flow

interface DebtLocalDataSource {
    fun getActiveByAccount(accountId: String): Flow<List<Debt>>
    fun getActive(): Flow<List<Debt>>
    fun getAll(): Flow<List<Debt>>
    fun getByAccount(accountId: String): Flow<List<Debt>>
    fun getById(id: String): Flow<Debt?>
    fun getTotalByDirection(direction: String): Flow<Double>
    fun getTotalByDirectionAndAccount(accountId: String, direction: String): Flow<Double>
    suspend fun insert(entity: Debt): Result<Unit>
    suspend fun update(entity: Debt): Result<Unit>
    suspend fun markAsPaid(id: String): Result<Unit>
    suspend fun archive(id: String): Result<Unit>
    suspend fun unarchive(id: String): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
}
