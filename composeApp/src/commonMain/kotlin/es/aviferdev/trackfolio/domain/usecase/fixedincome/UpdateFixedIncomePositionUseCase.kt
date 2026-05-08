package es.aviferdev.trackfolio.domain.usecase.fixedincome

import es.aviferdev.trackfolio.domain.repository.FixedIncomeRepository

class UpdateFixedIncomePositionUseCase(
    private val repository: FixedIncomeRepository
) {
    suspend operator fun invoke(position: es.aviferdev.trackfolio.domain.model.FixedIncomePosition): Result<Unit> =
        repository.update(position)
}