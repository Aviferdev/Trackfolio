package es.aviferdev.n3to.data.datasource.goal

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.n3to.data.database.N3toDatabase
import es.aviferdev.n3to.domain.model.MonthlyGoal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class GoalLocalDataSourceImpl(
    private val database: N3toDatabase
) : GoalLocalDataSource {

    private val queries = database.monthlyGoalQueries

    override fun getByAccountYear(accountId: String, year: Int): Flow<List<MonthlyGoal>> =
        queries.getByAccountYear(accountId, year.toLong())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.toDomain() } }

    override fun getByAccountYearMonth(
        accountId: String,
        year: Int,
        month: Int
    ): Flow<MonthlyGoal?> =
        queries.getByAccountYearMonth(accountId, year.toLong(), month.toLong())
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { row -> row?.toDomain() }

    override fun getBaseGoal(accountId: String, year: Int): Flow<MonthlyGoal?> =
        queries.getBaseByAccountYear(accountId, year.toLong())
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { row -> row?.toDomain() }

    override fun getOverrides(accountId: String, year: Int): Flow<List<MonthlyGoal>> =
        queries.getOverridesByAccountYear(accountId, year.toLong())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun upsert(goal: MonthlyGoal) {
        withContext(Dispatchers.IO) {
            queries.upsert(
                accountId = goal.accountId,
                year = goal.year.toLong(),
                month = goal.month.toLong(),
                savingsTarget = goal.savingsTarget,
                investmentTarget = goal.investmentTarget
            )
        }
    }

    override suspend fun delete(accountId: String, year: Int, month: Int) {
        withContext(Dispatchers.IO) {
            queries.deleteByAccountYearMonth(accountId, year.toLong(), month.toLong())
        }
    }

    override suspend fun deleteByAccountYear(accountId: String, year: Int) {
        withContext(Dispatchers.IO) {
            queries.deleteByAccountYear(accountId, year.toLong())
        }
    }

    private fun es.aviferdev.n3to.data.database.MonthlyGoalEntity.toDomain(): MonthlyGoal =
        MonthlyGoal(
            accountId = accountId,
            year = year.toInt(),
            month = month.toInt(),
            savingsTarget = savingsTarget,
            investmentTarget = investmentTarget
        )
}
