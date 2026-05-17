package es.aviferdev.n3to.domain.usecase.goal

import es.aviferdev.n3to.domain.model.MonthlyGoal
import es.aviferdev.n3to.domain.repository.GoalRepository

class SaveMonthlyGoalUseCase(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(goal: MonthlyGoal) {
        repository.saveMonthlyGoal(goal)
    }
}
