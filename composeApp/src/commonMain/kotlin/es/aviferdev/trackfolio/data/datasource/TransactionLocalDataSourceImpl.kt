package es.aviferdev.trackfolio.data.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.trackfolio.data.database.TrackfolioDatabase
import es.aviferdev.trackfolio.data.database.TransactionEntity
import es.aviferdev.trackfolio.data.database.mapper.toDomain
import es.aviferdev.trackfolio.domain.model.AnnualSummary
import es.aviferdev.trackfolio.domain.model.MonthlyTotals
import es.aviferdev.trackfolio.domain.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TransactionLocalDataSourceImpl(
    private val database: TrackfolioDatabase
) : TransactionLocalDataSource {

    private val queries = database.transactionQueries

    override fun getByMonthAndAccount(
        accountId: String, year: String, month: String
    ): Flow<List<Transaction>> =
        queries.selectByMonthAndAccount(accountId, year, month)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getMonthlyTotalsByAccount(
        accountId: String, year: String, month: String
    ): Flow<MonthlyTotals> =
        queries.getMonthlyTotalsByAccount(accountId, year, month)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { row ->
                MonthlyTotals(
                    year         = year,
                    month        = month,
                    totalIncome  = row?.totalIncome ?: 0.0,
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
                    year                = year,
                    totalIncome         = current?.totalIncome ?: 0.0,
                    totalExpense        = current?.totalExpense ?: 0.0,
                    previousYearIncome  = prev?.totalIncome ?: 0.0,
                    previousYearExpense = prev?.totalExpense ?: 0.0
                )
            }
    }

    override fun getRecentByAccount(accountId: String, limit: Long): Flow<List<Transaction>> =
        queries.selectRecentByAccount(accountId, limit)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getMonthlyBreakdown(accountId: String, year: String): Flow<List<MonthlyTotals>> =
        queries.getMonthlyBreakdownByAccount(accountId, year)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    MonthlyTotals(
                        year         = year,
                        month        = row.month ?: "01",
                        totalIncome  = row.totalIncome,
                        totalExpense = row.totalExpense
                    )
                }
            }

    override fun getIncomeByYear(accountId: String, year: String): Flow<List<Transaction>> =
        queries.getIncomeByYear(accountId, year)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun insert(entity: TransactionEntity): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.insert(
                    id                   = entity.id,
                    accountId            = entity.accountId,
                    amount               = entity.amount,
                    type                 = entity.type,
                    categoryId           = entity.categoryId,
                    date                 = entity.date,
                    notes                = entity.notes,
                    createdAt            = entity.createdAt,
                    incomeType           = entity.incomeType,
                    grossAmount          = entity.grossAmount,
                    irpfPercent          = entity.irpfPercent,
                    socialSecurityAmount = entity.socialSecurityAmount,
                    commissionAmount     = entity.commissionAmount,
                    issuerId             = entity.issuerId,
                    issuerName           = entity.issuerName
                )
            }
        }

    override suspend fun update(entity: TransactionEntity): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.update(
                    accountId            = entity.accountId,
                    amount               = entity.amount,
                    type                 = entity.type,
                    categoryId           = entity.categoryId,
                    date                 = entity.date,
                    notes                = entity.notes,
                    incomeType           = entity.incomeType,
                    grossAmount          = entity.grossAmount,
                    irpfPercent          = entity.irpfPercent,
                    socialSecurityAmount = entity.socialSecurityAmount,
                    commissionAmount     = entity.commissionAmount,
                    issuerId             = entity.issuerId,
                    issuerName           = entity.issuerName,
                    id                   = entity.id
                )
            }
        }

    override suspend fun delete(id: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.delete(id)
            }
        }
}
