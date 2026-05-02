package es.aviferdev.trackfolio.domain.usecase.debt

import es.aviferdev.trackfolio.domain.model.Debt
import es.aviferdev.trackfolio.domain.repository.DebtRepository

class SaveDebtUseCase(private val repository: DebtRepository) {
    suspend operator fun invoke(debt: Debt): Result<Unit> = repository.save(debt)
}
