package es.aviferdev.n3to.domain.repository

import es.aviferdev.n3to.domain.model.MonthlyGoal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    /** Todos los objetivos de un año (base + overrides). */
    fun getMonthlyGoals(accountId: String, year: Int): Flow<List<MonthlyGoal>>

    /** Objetivo de un mes concreto (puede ser null). */
    fun getMonthlyGoal(accountId: String, year: Int, month: Int): Flow<MonthlyGoal?>

    /** Objetivo base del año (month="00"). */
    fun getBaseGoal(accountId: String, year: Int): Flow<MonthlyGoal?>

    /** Overrides del año (months "01".."12"). */
    fun getOverrides(accountId: String, year: Int): Flow<List<MonthlyGoal>>

    /**
     * Objetivo efectivo para un mes: si hay override específico, lo usa;
     * si no, usa el base (month="00"). Si no hay base, devuelve null.
     */
    fun getEffectiveMonthlyGoal(accountId: String, year: Int, month: Int): Flow<MonthlyGoal?>

    /** Guarda o actualiza un objetivo. */
    suspend fun saveMonthlyGoal(goal: MonthlyGoal)

    /**
     * Guarda el objetivo base y los overrides de forma atómica.
     * Elimina todos los datos previos del año y guarda el base + overrides.
     */
    suspend fun saveBaseAndOverrides(
        accountId: String,
        year: Int,
        baseGoal: MonthlyGoal?,
        overrides: List<MonthlyGoal>
    )

    /** Elimina el objetivo de un mes concreto. */
    suspend fun deleteMonthlyGoal(accountId: String, year: Int, month: Int)
}
