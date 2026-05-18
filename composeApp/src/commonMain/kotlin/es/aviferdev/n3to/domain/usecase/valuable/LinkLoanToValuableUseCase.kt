package es.aviferdev.n3to.domain.usecase.valuable

import es.aviferdev.n3to.domain.repository.ValuableRepository

class LinkLoanToValuableUseCase(
    private val repository: ValuableRepository
) {
    suspend operator fun invoke(valuableId: String, loanId: String): Result<Unit> =
        repository.linkLoan(valuableId, loanId)
}
