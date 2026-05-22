package es.aviferdev.n3to.domain.usecase.category

import es.aviferdev.n3to.domain.repository.CategoryRepository

class RenameCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(id: String, newName: String): Result<Unit> =
        repository.rename(id, newName)
}
