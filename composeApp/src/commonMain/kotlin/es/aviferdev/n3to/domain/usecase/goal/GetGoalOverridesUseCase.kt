package es.aviferdev.n3to.domain.usecase.goal

import es.aviferdev.n3to.domain.model.MonthlyGoal
import es.aviferdev.n3to.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow

class GetGoalOverridesUseCase(private val repository: GoalRepository) {
    operator fun invoke(accountId: String, year: Int): Flow<List<MonthlyGoal>> =
        repository.getOverrides(accountId, year)
}
