package es.aviferdev.n3to.domain.usecase.category

import es.aviferdev.n3to.domain.repository.CategoryRepository

class DeleteCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(categoryId: String): Result<Unit> =
        repository.archive(categoryId)
}
