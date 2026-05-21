package es.aviferdev.n3to.data.datasource.transaction

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.n3to.data.database.N3toDatabase
import es.aviferdev.n3to.data.database.TransactionEntity
import es.aviferdev.n3to.data.database.mapper.toDomain
import es.aviferdev.n3to.domain.model.AnnualSummary
import es.aviferdev.n3to.domain.model.CategoryBreakdown
import es.aviferdev.n3to.domain.model.IncomeTypeBreakdown
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.MonthlyTotals
import es.aviferdev.n3to.domain.model.TaxLine
import es.aviferdev.n3to.domain.model.TaxRole
import es.aviferdev.n3to.domain.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TransactionLocalDataSourceImpl(
    private val database: N3toDatabase
) : TransactionLocalDataSource {

    private val queries = database.transactionQueries
    private val taxLineQueries = database.taxLineQueries

    private fun loadTaxLines(transactionId: String): List<TaxLine> =
        taxLineQueries.selectByTransaction(transactionId).executeAsList().map { entity ->
            TaxLine(
                name = entity.name,
                role = TaxRole.valueOf(entity.role),
                percent = entity.percent,
                amount = entity.amount
            )
        }

    private fun List<TransactionEntity>.toDomainWithTaxLines(): List<Transaction> =
        map { entity -> entity.toDomain(loadTaxLines(entity.id)) }

    override fun getById(id: String): Flow<Transaction?> =
        queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain(loadTaxLines(it.id)) }

    override fun getByMonthAndAccount(
        accountId: String, year: String, month: String
    ): Flow<List<Transaction>> =
        queries.selectByMonthAndAccount(accountId, year, month)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.toDomainWithTaxLines() }

    override fun getMonthlyTotalsByAccount(
        accountId: String, year: String, month: String
    ): Flow<MonthlyTotals> =
        queries.getMonthlyTotalsByAccount(accountId, year, month)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { row ->
                MonthlyTotals(
                    year = year,
                    month = month,
                    totalIncome = row?.totalIncome ?: 0.0,
                    totalExpense = row?.totalExpense ?: 0.0
                )
            }

    override fun getAnnualTotalsByAccount(
        accountId: String, year: String
    ): Flow<AnnualSummary> {
        val prevYear = (year.toInt() - 1).toString()
        return queries.getAnnualTotalsByAccount(accountId, year)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { current ->
                val prev = queries.getAnnualTotalsByAccount(accountId, prevYear)
                    .executeAsOneOrNull()
                AnnualSummary(
                    year = year,
                    totalIncome = current?.totalIncome ?: 0.0,
                    totalExpense = current?.totalExpense ?: 0.0,
                    previousYearIncome = prev?.totalIncome ?: 0.0,
                    previousYearExpense = prev?.totalExpense ?: 0.0
                )
            }
    }

    override fun getRecentByAccount(accountId: String, limit: Long): Flow<List<Transaction>> =
        queries.selectRecentByAccount(accountId, limit)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.toDomainWithTaxLines() }

    override fun getMonthlyBreakdown(accountId: String, year: String): Flow<List<MonthlyTotals>> =
        queries.getMonthlyBreakdownByAccount(accountId, year)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    MonthlyTotals(
                        year = year,
                        month = row.month,
                        totalIncome = row.totalIncome,
                        totalExpense = row.totalExpense
                    )
                }
            }

    override fun getIncomeByYear(accountId: String, year: String): Flow<List<Transaction>> =
        queries.getIncomeByYear(accountId, year)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.toDomainWithTaxLines() }

    override suspend fun insert(entity: TransactionEntity): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.insert(
                    id = entity.id,
                    accountId = entity.accountId,
                    amount = entity.amount,
                    type = entity.type,
                    categoryId = entity.categoryId,
                    date = entity.date,
                    notes = entity.notes,
                    createdAt = entity.createdAt,
                    excludeFromFiscal = entity.excludeFromFiscal,
                    incomeType = entity.incomeType,
                    grossAmount = entity.grossAmount,
                    commissionAmount = entity.commissionAmount,
                    issuerId = entity.issuerId,
                    issuerName = entity.issuerName,
                    originalCurrency = entity.originalCurrency,
                    originalAmount = entity.originalAmount,
                    exchangeRate = entity.exchangeRate,
                    linkedAssetTransactionId = entity.linkedAssetTransactionId,
                    linkedLoanId = entity.linkedLoanId,
                    linkedPropertyId = entity.linkedPropertyId,
                    linkedValuableId = entity.linkedValuableId
                )
            }
        }

    override suspend fun update(entity: TransactionEntity): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.update(
                    accountId = entity.accountId,
                    amount = entity.amount,
                    type = entity.type,
                    categoryId = entity.categoryId,
                    date = entity.date,
                    notes = entity.notes,
                    excludeFromFiscal = entity.excludeFromFiscal,
                    incomeType = entity.incomeType,
                    grossAmount = entity.grossAmount,
                    commissionAmount = entity.commissionAmount,
                    issuerId = entity.issuerId,
                    issuerName = entity.issuerName,
                    originalCurrency = entity.originalCurrency,
                    originalAmount = entity.originalAmount,
                    exchangeRate = entity.exchangeRate,
                    linkedAssetTransactionId = entity.linkedAssetTransactionId,
                    linkedLoanId = entity.linkedLoanId,
                    linkedPropertyId = entity.linkedPropertyId,
                    linkedValuableId = entity.linkedValuableId,
                    id = entity.id
                )
            }
        }

    override suspend fun delete(id: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.delete(id)
            }
        }

    override suspend fun deleteByLinkedAssetTransaction(assetTxId: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.deleteByLinkedAssetTransaction(assetTxId)
            }
        }

    override fun getByLinkedAssetTransaction(assetTxId: String): Flow<Transaction?> =
        queries.selectByLinkedAssetTransaction(assetTxId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain(it.id.let(::loadTaxLines)) }

    override fun getOldestDate(accountId: String): Flow<Long?> =
        queries.getOldestDateByAccount(accountId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.oldestDate }

    override fun getDividendsByAsset(assetId: String): Flow<List<Transaction>> =
        queries.getDividendsByAssetId(assetId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.toDomainWithTaxLines() }

    override fun getByLinkedProperty(propertyId: String): Flow<List<Transaction>> =
        queries.selectByLinkedProperty(propertyId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.toDomainWithTaxLines() }

    override fun getByLinkedValuable(valuableId: String): Flow<List<Transaction>> =
        queries.selectByLinkedValuable(valuableId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.toDomainWithTaxLines() }

    override fun getExpensesByCategoryPerYear(
        accountId: String,
        year: String
    ): Flow<List<CategoryBreakdown>> =
        queries.getExpensesByCategoryPerYear(accountId, year)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    CategoryBreakdown(
                        categoryId = row.categoryId,
                        categoryName = row.categoryName,
                        amount = row.totalAmount ?: 0.0
                    )
                }
            }

    override fun getIncomeByTypePerYear(
        accountId: String,
        year: String
    ): Flow<List<IncomeTypeBreakdown>> =
        queries.getIncomeByTypePerYear(accountId, year)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    val incomeType = IncomeType.fromName(row.incomeType)
                    IncomeTypeBreakdown(
                        incomeType = row.incomeType ?: "UNKNOWN",
                        label = incomeType?.label ?: "Otro",
                        emoji = incomeType?.emoji ?: "💰",
                        amount = row.totalAmount ?: 0.0
                    )
                }
            }

    override fun getExpensesByCategoryPerMonth(
        accountId: String,
        year: String,
        month: String
    ): Flow<List<CategoryBreakdown>> =
        queries.getExpensesByCategoryPerMonth(accountId, year, month)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    CategoryBreakdown(
                        categoryId = row.categoryId,
                        categoryName = row.categoryName,
                        amount = row.totalAmount ?: 0.0
                    )
                }
            }

    override fun getIncomeByTypePerMonth(
        accountId: String,
        year: String,
        month: String
    ): Flow<List<IncomeTypeBreakdown>> =
        queries.getIncomeByTypePerMonth(accountId, year, month)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    val incomeType = IncomeType.fromName(row.incomeType)
                    IncomeTypeBreakdown(
                        incomeType = row.incomeType ?: "UNKNOWN",
                        label = incomeType?.label ?: "Otro",
                        emoji = incomeType?.emoji ?: "💰",
                        amount = row.totalAmount ?: 0.0
                    )
                }
            }
}
