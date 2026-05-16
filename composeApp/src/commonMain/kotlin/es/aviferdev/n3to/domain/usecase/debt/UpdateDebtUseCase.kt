package es.aviferdev.n3to.domain.usecase.debt

import es.aviferdev.n3to.domain.model.Debt
import es.aviferdev.n3to.domain.repository.DebtRepository

class UpdateDebtUseCase(private val repository: DebtRepository) {
    suspend operator fun invoke(debt: Debt): Result<Unit> = repository.update(debt)
}
