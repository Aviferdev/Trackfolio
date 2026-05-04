package es.aviferdev.trackfolio.domain.usecase.debt

import es.aviferdev.trackfolio.domain.repository.DebtRepository

class DeleteDebtUseCase(private val repository: DebtRepository) {
    suspend operator fun invoke(id: String): Result<Unit> =
        repository.delete(id)
}
