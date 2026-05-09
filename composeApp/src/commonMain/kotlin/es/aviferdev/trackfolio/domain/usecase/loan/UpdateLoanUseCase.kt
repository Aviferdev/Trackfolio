package es.aviferdev.trackfolio.domain.usecase.loan

import es.aviferdev.trackfolio.domain.model.Loan
import es.aviferdev.trackfolio.domain.repository.LoanRepository

class UpdateLoanUseCase(
    private val repository: LoanRepository
) {
    suspend operator fun invoke(loan: Loan): Result<Unit> =
        repository.update(loan)
}
