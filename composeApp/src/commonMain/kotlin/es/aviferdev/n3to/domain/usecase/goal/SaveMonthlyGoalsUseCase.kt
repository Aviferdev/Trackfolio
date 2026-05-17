package es.aviferdev.n3to.domain.usecase.goal

import es.aviferdev.n3to.domain.model.MonthlyGoal
import es.aviferdev.n3to.domain.repository.GoalRepository

/**
 * Guarda múltiples objetivos a la vez (batch upsert).
 * Útil para "aplicar a todos los meses" o inicializar el año completo.
 */
class SaveMonthlyGoalsUseCase(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(goals: List<MonthlyGoal>) {
        goals.forEach { repository.saveMonthlyGoal(it) }
    }
}
