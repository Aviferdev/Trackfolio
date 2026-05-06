package es.aviferdev.trackfolio.data.datasource

import es.aviferdev.trackfolio.data.database.TransactionEntity
import es.aviferdev.trackfolio.domain.model.MonthlyTotals
import es.aviferdev.trackfolio.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionLocalDataSource {
    fun getByMonthAndAccount(accountId: String, year: String, month: String): Flow<List<Transaction>>
    fun getMonthlyTotalsByAccount(accountId: String, year: String, month: String): Flow<MonthlyTotals>
    fun getAnnualTotalsByAccount(accountId: String, year: String): Flow<es.aviferdev.trackfolio.domain.model.AnnualSummary>
    fun getRecentByAccount(accountId: String, limit: Long): Flow<List<Transaction>>
    fun getMonthlyBreakdown(accountId: String, year: String): Flow<List<MonthlyTotals>>
    /** Todos los ingresos del año indicado para el informe fiscal. */
    fun getIncomeByYear(accountId: String, year: String): Flow<List<Transaction>>
    // CRUD
    suspend fun insert(entity: TransactionEntity): Result<Unit>
    suspend fun update(entity: TransactionEntity): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
}
