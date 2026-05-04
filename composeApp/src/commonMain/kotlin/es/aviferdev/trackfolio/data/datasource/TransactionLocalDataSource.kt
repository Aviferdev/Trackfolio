package es.aviferdev.trackfolio.data.datasource

import es.aviferdev.trackfolio.data.database.GetAnnualTotals
import es.aviferdev.trackfolio.data.database.GetMonthlyTotals
import es.aviferdev.trackfolio.data.database.TransactionEntity
import es.aviferdev.trackfolio.domain.model.AnnualSummary
import es.aviferdev.trackfolio.domain.model.MonthlyTotals
import es.aviferdev.trackfolio.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionLocalDataSource {
    // Filtradas por cuenta (Sprint 9)
    fun getByMonthAndAccount(accountId: String, year: String, month: String): Flow<List<Transaction>>
    fun getMonthlyTotalsByAccount(accountId: String, year: String, month: String): Flow<MonthlyTotals>
    fun getAnnualTotalsByAccount(accountId: String, year: String): Flow<AnnualSummary>
    fun getRecentByAccount(accountId: String, limit: Long): Flow<List<Transaction>>
    fun getMonthlyBreakdown(accountId: String, year: String): Flow<List<MonthlyTotals>>
    // CRUD
    suspend fun insert(entity: TransactionEntity): Result<Unit>
    suspend fun update(entity: TransactionEntity): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
}
