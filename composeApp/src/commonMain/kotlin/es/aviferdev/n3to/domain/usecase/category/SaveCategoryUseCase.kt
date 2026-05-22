package es.aviferdev.n3to.domain.usecase.category

import es.aviferdev.n3to.domain.model.Category
import es.aviferdev.n3to.domain.repository.CategoryRepository

class SaveCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(category: Category): Result<Unit> =
        repository.save(category)
}
