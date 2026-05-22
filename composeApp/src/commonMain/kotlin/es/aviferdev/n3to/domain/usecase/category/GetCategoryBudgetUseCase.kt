package es.aviferdev.n3to.domain.usecase.category

import es.aviferdev.n3to.data.database.CategoryBudgetEntity
import es.aviferdev.n3to.domain.repository.CategoryBudgetRepository
import kotlinx.coroutines.flow.Flow

class GetCategoryBudgetUseCase(private val repository: CategoryBudgetRepository) {
    operator fun invoke(categoryId: String): Flow<CategoryBudgetEntity?> =
        repository.getBudgetByCategoryId(categoryId)
}
