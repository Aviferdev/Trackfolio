package es.aviferdev.n3to.data.repository

import es.aviferdev.n3to.data.datasource.goal.GoalLocalDataSource
import es.aviferdev.n3to.domain.model.MonthlyGoal
import es.aviferdev.n3to.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class GoalRepositoryImpl(
    private val dataSource: GoalLocalDataSource
) : GoalRepository {

    override fun getMonthlyGoals(accountId: String, year: String): Flow<List<MonthlyGoal>> =
        dataSource.getByAccountYear(accountId, year)

    override fun getMonthlyGoal(
        accountId: String,
        year: String,
        month: String
    ): Flow<MonthlyGoal?> =
        dataSource.getByAccountYearMonth(accountId, year, month)

    override fun getBaseGoal(accountId: String, year: String): Flow<MonthlyGoal?> =
        dataSource.getBaseGoal(accountId, year)

    override fun getOverrides(accountId: String, year: String): Flow<List<MonthlyGoal>> =
        dataSource.getOverrides(accountId, year)

    override fun getEffectiveMonthlyGoal(
        accountId: String,
        year: String,
        month: String
    ): Flow<MonthlyGoal?> = flow {
        // Primero buscar override específico
        val override = dataSource.getByAccountYearMonth(accountId, year, month).first()
        if (override != null) {
            emit(override)
        } else {
            // Si no hay override, usar el base
            val base = dataSource.getBaseGoal(accountId, year).first()
            if (base != null) {
                // Devolver el base pero con el mes solicitado
                emit(base.copy(month = month))
            } else {
                emit(null)
            }
        }
    }

    override suspend fun saveMonthlyGoal(goal: MonthlyGoal) =
        dataSource.upsert(goal)

    override suspend fun saveBaseAndOverrides(
        accountId: String,
        year: String,
        baseGoal: MonthlyGoal?,
        overrides: List<MonthlyGoal>
    ) {
        // Limpiar todos los datos del año
        dataSource.deleteByAccountYear(accountId, year)

        // Guardar el base si existe
        if (baseGoal != null) {
            dataSource.upsert(baseGoal)
        }

        // Guardar los overrides
        overrides.forEach { dataSource.upsert(it) }
    }

    override suspend fun deleteMonthlyGoal(accountId: String, year: String, month: String) =
        dataSource.delete(accountId, year, month)
}
