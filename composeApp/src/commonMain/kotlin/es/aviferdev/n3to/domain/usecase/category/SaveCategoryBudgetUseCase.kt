package es.aviferdev.n3to.domain.usecase.category

import es.aviferdev.n3to.domain.model.LimitType
import es.aviferdev.n3to.domain.repository.CategoryBudgetRepository

class SaveCategoryBudgetUseCase(private val repository: CategoryBudgetRepository) {
    suspend operator fun invoke(
        categoryId: String,
        annualLimit: Double,
        limitType: LimitType
    ): Result<Unit> = repository.saveBudget(categoryId, annualLimit, limitType)
}
