package es.aviferdev.trackfolio.domain.repository

import es.aviferdev.trackfolio.domain.model.AnnualSummary
import es.aviferdev.trackfolio.domain.model.MonthlyTotals
import es.aviferdev.trackfolio.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAll(): Flow<List<Transaction>>
    fun getByAccount(accountId: String): Flow<List<Transaction>>
    fun getByMonth(year: String, month: String): Flow<List<Transaction>>
    fun getMonthlyTotals(year: String, month: String): Flow<MonthlyTotals>
    fun getAnnualSummary(year: String): Flow<AnnualSummary>
    suspend fun save(transaction: Transaction): Result<Unit>
    suspend fun update(transaction: Transaction): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
}
