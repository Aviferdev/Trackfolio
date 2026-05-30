package es.aviferdev.n3to.data.datasource.goal

import es.aviferdev.n3to.domain.model.MonthlyGoal
import kotlinx.coroutines.flow.Flow

interface GoalLocalDataSource {
    fun getByAccountYear(accountId: String, year: Int): Flow<List<MonthlyGoal>>
    fun getByAccountYearMonth(accountId: String, year: Int, month: Int): Flow<MonthlyGoal?>

    /** Obtiene el objetivo base (month="00"). */
    fun getBaseGoal(accountId: String, year: Int): Flow<MonthlyGoal?>

    /** Obtiene los overrides (months "01".."12"). */
    fun getOverrides(accountId: String, year: Int): Flow<List<MonthlyGoal>>
    suspend fun upsert(goal: MonthlyGoal)
    suspend fun delete(accountId: String, year: Int, month: Int)

    /** Elimina todos los objetivos de un año (útil antes de guardar batch). */
    suspend fun deleteByAccountYear(accountId: String, year: Int)
}
