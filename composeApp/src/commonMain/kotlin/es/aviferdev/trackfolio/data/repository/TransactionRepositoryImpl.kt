package es.aviferdev.trackfolio.data.repository

import es.aviferdev.trackfolio.data.database.mapper.toEntity
import es.aviferdev.trackfolio.data.datasource.TransactionLocalDataSource
import es.aviferdev.trackfolio.domain.model.AnnualSummary
import es.aviferdev.trackfolio.domain.model.MonthlyTotals
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class TransactionRepositoryImpl(
    private val dataSource: TransactionLocalDataSource
) : TransactionRepository {

    override fun getTransactionsByMonthAndAccount(
        accountId: String, year: String, month: String
    ): Flow<List<Transaction>> =
        dataSource.getByMonthAndAccount(accountId, year, month)

    override fun getMonthlyTotalsByAccount(
        accountId: String, year: String, month: String
    ): Flow<MonthlyTotals> =
        dataSource.getMonthlyTotalsByAccount(accountId, year, month)

    override fun getAnnualSummaryByAccount(
        accountId: String, year: String
    ): Flow<AnnualSummary> =
        dataSource.getAnnualTotalsByAccount(accountId, year)

    override fun getRecentTransactionsByAccount(
        accountId: String, limit: Long
    ): Flow<List<Transaction>> =
        dataSource.getRecentByAccount(accountId, limit)

    override fun getMonthlyBreakdown(accountId: String, year: String): Flow<List<MonthlyTotals>> =
        dataSource.getMonthlyBreakdown(accountId, year)

    override fun getIncomeByYear(accountId: String, year: String): Flow<List<Transaction>> =
        dataSource.getIncomeByYear(accountId, year)

    override suspend fun saveTransaction(transaction: Transaction): Result<Unit> =
        dataSource.insert(transaction.toEntity())

    override suspend fun updateTransaction(transaction: Transaction): Result<Unit> =
        dataSource.update(transaction.toEntity())

    override suspend fun deleteTransaction(id: String): Result<Unit> =
        dataSource.delete(id)
}
