package es.aviferdev.n3to.data.repository.transaction

import es.aviferdev.n3to.data.database.mapper.toEntity
import es.aviferdev.n3to.data.datasource.transaction.TransactionLocalDataSource
import es.aviferdev.n3to.domain.model.AnnualSummary
import es.aviferdev.n3to.domain.model.CategoryBreakdown
import es.aviferdev.n3to.domain.model.IncomeTypeBreakdown
import es.aviferdev.n3to.domain.model.MonthlyTotals
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.repository.TransactionRepository
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

    override fun getExpensesByCategoryPerYear(
        accountId: String,
        year: String
    ): Flow<List<CategoryBreakdown>> =
        dataSource.getExpensesByCategoryPerYear(accountId, year)

    override fun getIncomeByTypePerYear(
        accountId: String,
        year: String
    ): Flow<List<IncomeTypeBreakdown>> =
        dataSource.getIncomeByTypePerYear(accountId, year)

    override fun getTransactionById(id: String): Flow<Transaction?> =
        dataSource.getById(id)

    override suspend fun saveTransaction(transaction: Transaction): Result<Unit> =
        dataSource.insert(transaction.toEntity())

    override suspend fun updateTransaction(transaction: Transaction): Result<Unit> =
        dataSource.update(transaction.toEntity())

    override suspend fun deleteTransaction(id: String): Result<Unit> =
        dataSource.delete(id)

    override suspend fun deleteByLinkedAssetTransaction(assetTransactionId: String): Result<Unit> =
        dataSource.deleteByLinkedAssetTransaction(assetTransactionId)

    override fun getByLinkedAssetTransaction(assetTransactionId: String): Flow<Transaction?> =
        dataSource.getByLinkedAssetTransaction(assetTransactionId)

    override fun getOldestDate(accountId: String): Flow<Long?> =
        dataSource.getOldestDate(accountId)

    override fun getDividendsByAsset(assetId: String): Flow<List<Transaction>> =
        dataSource.getDividendsByAsset(assetId)

    override fun getByLinkedProperty(propertyId: String): Flow<List<Transaction>> =
        dataSource.getByLinkedProperty(propertyId)

    override fun getByLinkedValuable(valuableId: String): Flow<List<Transaction>> =
        dataSource.getByLinkedValuable(valuableId)
}