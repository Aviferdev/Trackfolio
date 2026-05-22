package es.aviferdev.n3to.domain.usecase.category

import es.aviferdev.n3to.domain.repository.CategoryBudgetRepository

class DeleteCategoryBudgetUseCase(private val repository: CategoryBudgetRepository) {
    suspend operator fun invoke(categoryId: String): Result<Unit> =
        repository.deleteBudget(categoryId)
}
