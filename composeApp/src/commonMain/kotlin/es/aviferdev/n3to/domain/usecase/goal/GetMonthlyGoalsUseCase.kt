package es.aviferdev.n3to.domain.usecase.goal

import es.aviferdev.n3to.domain.model.MonthlyGoal
import es.aviferdev.n3to.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow

class GetMonthlyGoalsUseCase(
    private val repository: GoalRepository
) {
    /** Todos los objetivos de un año. */
    operator fun invoke(accountId: String, year: Int): Flow<List<MonthlyGoal>> =
        repository.getMonthlyGoals(accountId, year)

    /** Objetivo de un mes concreto. */
    operator fun invoke(accountId: String, year: Int, month: Int): Flow<MonthlyGoal?> =
        repository.getMonthlyGoal(accountId, year, month)
}
