package es.aviferdev.n3to.domain.usecase.debt

import es.aviferdev.n3to.domain.repository.DebtRepository

class MarkDebtAsPaidUseCase(private val repository: DebtRepository) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.markAsPaid(id)
}
