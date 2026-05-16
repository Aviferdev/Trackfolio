package es.aviferdev.n3to.domain.usecase.loan

import es.aviferdev.n3to.domain.model.Loan
import es.aviferdev.n3to.domain.repository.LoanRepository

class UpdateLoanUseCase(
    private val repository: LoanRepository
) {
    suspend operator fun invoke(loan: Loan): Result<Unit> =
        repository.update(loan)
}
