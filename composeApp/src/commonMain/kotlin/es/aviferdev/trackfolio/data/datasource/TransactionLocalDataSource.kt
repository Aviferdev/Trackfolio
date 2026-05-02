package es.aviferdev.trackfolio.data.datasource

import es.aviferdev.trackfolio.data.database.TransactionEntity
import es.aviferdev.trackfolio.data.database.GetMonthlyTotals
import es.aviferdev.trackfolio.data.database.GetAnnualTotals
import kotlinx.coroutines.flow.Flow

interface TransactionLocalDataSource {
    fun getAll(): Flow<List<TransactionEntity>>
    fun getByAccount(accountId: String): Flow<List<TransactionEntity>>
    fun getByMonth(year: String, month: String): Flow<List<TransactionEntity>>
    fun getMonthlyTotals(year: String, month: String): Flow<GetMonthlyTotals>
    fun getAnnualTotals(year: String): Flow<GetAnnualTotals>
    suspend fun insert(entity: TransactionEntity): Result<Unit>
    suspend fun update(entity: TransactionEntity): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
}
