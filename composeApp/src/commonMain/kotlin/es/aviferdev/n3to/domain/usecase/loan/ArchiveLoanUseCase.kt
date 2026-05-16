package es.aviferdev.n3to.domain.usecase.loan

import es.aviferdev.n3to.domain.repository.LoanRepository

class ArchiveLoanUseCase(
    private val repository: LoanRepository
) {
    suspend operator fun invoke(loanId: String): Result<Unit> =
        repository.archive(loanId)
}
