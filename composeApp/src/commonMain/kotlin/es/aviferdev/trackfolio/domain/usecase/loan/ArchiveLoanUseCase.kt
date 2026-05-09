package es.aviferdev.trackfolio.domain.usecase.loan

import es.aviferdev.trackfolio.domain.repository.LoanRepository

class ArchiveLoanUseCase(
    private val repository: LoanRepository
) {
    suspend operator fun invoke(loanId: String): Result<Unit> =
        repository.archive(loanId)
}
