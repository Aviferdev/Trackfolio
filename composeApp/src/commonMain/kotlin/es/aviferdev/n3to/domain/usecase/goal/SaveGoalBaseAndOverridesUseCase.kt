package es.aviferdev.n3to.domain.usecase.goal

import es.aviferdev.n3to.domain.model.MonthlyGoal
import es.aviferdev.n3to.domain.repository.GoalRepository

class SaveGoalBaseAndOverridesUseCase(private val repository: GoalRepository) {
    suspend operator fun invoke(
        accountId: String,
        year: Int,
        baseGoal: MonthlyGoal?,
        overrides: List<MonthlyGoal>
    ) = repository.saveBaseAndOverrides(accountId, year, baseGoal, overrides)
}
