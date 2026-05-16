package es.aviferdev.n3to.domain.usecase.loan

import es.aviferdev.n3to.domain.model.Loan
import es.aviferdev.n3to.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow

class GetLoansByAccountUseCase(
    private val repository: LoanRepository
) {
    operator fun invoke(accountId: String): Flow<List<Loan>> =
        repository.getActiveByAccount(accountId)
}
