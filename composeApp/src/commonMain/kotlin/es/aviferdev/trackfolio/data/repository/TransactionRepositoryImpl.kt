package es.aviferdev.trackfolio.data.repository

import es.aviferdev.trackfolio.data.database.mapper.toDomain
import es.aviferdev.trackfolio.data.database.mapper.toEntity
import es.aviferdev.trackfolio.data.datasource.TransactionLocalDataSource
import es.aviferdev.trackfolio.domain.model.AnnualSummary
import es.aviferdev.trackfolio.domain.model.MonthlyTotals
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl(
    private val dataSource: TransactionLocalDataSource
) : TransactionRepository {

    override fun getAll(): Flow<List<Transaction>> =
        dataSource.getAll().map { it.map { entity -> entity.toDomain() } }

    override fun getByAccount(accountId: String): Flow<List<Transaction>> =
        dataSource.getByAccount(accountId).map { it.map { entity -> entity.toDomain() } }

    override fun getByMonth(year: String, month: String): Flow<List<Transaction>> =
        dataSource.getByMonth(year, month).map { it.map { entity -> entity.toDomain() } }

    override fun getMonthlyTotals(year: String, month: String): Flow<MonthlyTotals> =
        dataSource.getMonthlyTotals(year, month).map { result ->
            MonthlyTotals(
                year = year,
                month = month,
                totalIncome = result.totalIncome,
                totalExpense = result.totalExpense
            )
        }

    override fun getAnnualSummary(year: String): Flow<AnnualSummary> {
        val previousYear = (year.toInt() - 1).toString()
        return combine(
            dataSource.getAnnualTotals(year),
            dataSource.getAnnualTotals(previousYear)
        ) { current, previous ->
            AnnualSummary(
                year = year,
                totalIncome = current.totalIncome,
                totalExpense = current.totalExpense,
                previousYearIncome = previous.totalIncome,
                previousYearExpense = previous.totalExpense
            )
        }
    }

    override suspend fun save(transaction: Transaction): Result<Unit> =
        dataSource.insert(transaction.toEntity())

    override suspend fun update(transaction: Transaction): Result<Unit> =
        dataSource.update(transaction.toEntity())

    override suspend fun delete(id: String): Result<Unit> =
        dataSource.delete(id)
}
