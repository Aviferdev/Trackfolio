package es.aviferdev.trackfolio.domain.repository

import es.aviferdev.trackfolio.domain.model.AnnualSummary
import es.aviferdev.trackfolio.domain.model.CategoryBreakdown
import es.aviferdev.trackfolio.domain.model.IncomeTypeBreakdown
import es.aviferdev.trackfolio.domain.model.MonthlyTotals
import es.aviferdev.trackfolio.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getTransactionsByMonthAndAccount(accountId: String, year: String, month: String): Flow<List<Transaction>>
    fun getMonthlyTotalsByAccount(accountId: String, year: String, month: String): Flow<MonthlyTotals>
    fun getAnnualSummaryByAccount(accountId: String, year: String): Flow<AnnualSummary>
    fun getRecentTransactionsByAccount(accountId: String, limit: Long = 5L): Flow<List<Transaction>>
    fun getMonthlyBreakdown(accountId: String, year: String): Flow<List<MonthlyTotals>>
    /** Ingresos del año con datos fiscales — para el informe IRPF. */
    fun getIncomeByYear(accountId: String, year: String): Flow<List<Transaction>>
    /** Desglose de gastos por categoría para un año. */
    fun getExpensesByCategoryPerYear(accountId: String, year: String): Flow<List<CategoryBreakdown>>
    /** Desglose de ingresos por tipo para un año. */
    fun getIncomeByTypePerYear(accountId: String, year: String): Flow<List<IncomeTypeBreakdown>>
    /** Obtiene una transacción por su ID. */
    fun getTransactionById(id: String): Flow<Transaction?>
    suspend fun saveTransaction(transaction: Transaction): Result<Unit>
    suspend fun updateTransaction(transaction: Transaction): Result<Unit>
    suspend fun deleteTransaction(id: String): Result<Unit>
    suspend fun deleteByLinkedAssetTransaction(assetTransactionId: String): Result<Unit>
    fun getByLinkedAssetTransaction(assetTransactionId: String): Flow<Transaction?>
    fun getOldestDate(accountId: String): Flow<Long?>
    fun getDividendsByAsset(assetId: String): Flow<List<Transaction>>
}
