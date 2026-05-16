package es.aviferdev.n3to.domain.usecase.fixedincome

import es.aviferdev.n3to.domain.repository.FixedIncomeRepository

class UpdateFixedIncomePositionUseCase(
    private val repository: FixedIncomeRepository
) {
    suspend operator fun invoke(position: es.aviferdev.n3to.domain.model.FixedIncomePosition): Result<Unit> =
        repository.update(position)
}