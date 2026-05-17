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

    override fun getByAccountYear(accountId: String, year: String): Flow<List<MonthlyGoal>> =
        queries.getByAccountYear(accountId, year)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.toDomain() } }

    override fun getByAccountYearMonth(
        accountId: String,
        year: String,
        month: String
    ): Flow<MonthlyGoal?> =
        queries.getByAccountYearMonth(accountId, year, month)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { row -> row?.toDomain() }

    override fun getBaseGoal(accountId: String, year: String): Flow<MonthlyGoal?> =
        queries.getBaseByAccountYear(accountId, year)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { row -> row?.toDomain() }

    override fun getOverrides(accountId: String, year: String): Flow<List<MonthlyGoal>> =
        queries.getOverridesByAccountYear(accountId, year)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun upsert(goal: MonthlyGoal) {
        withContext(Dispatchers.IO) {
            queries.upsert(
                accountId = goal.accountId,
                year = goal.year,
                month = goal.month,
                savingsTarget = goal.savingsTarget,
                investmentTarget = goal.investmentTarget
            )
        }
    }

    override suspend fun delete(accountId: String, year: String, month: String) {
        withContext(Dispatchers.IO) {
            queries.deleteByAccountYearMonth(accountId, year, month)
        }
    }

    override suspend fun deleteByAccountYear(accountId: String, year: String) {
        withContext(Dispatchers.IO) {
            queries.deleteByAccountYear(accountId, year)
        }
    }

    private fun es.aviferdev.n3to.data.database.MonthlyGoalEntity.toDomain(): MonthlyGoal =
        MonthlyGoal(
            accountId = accountId,
            year = year,
            month = month,
            savingsTarget = savingsTarget,
            investmentTarget = investmentTarget
        )
}
