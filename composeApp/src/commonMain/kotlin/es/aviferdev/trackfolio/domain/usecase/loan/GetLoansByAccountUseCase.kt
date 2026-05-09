package es.aviferdev.trackfolio.domain.usecase.loan

import es.aviferdev.trackfolio.domain.model.Loan
import es.aviferdev.trackfolio.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow

class GetLoansByAccountUseCase(
    private val repository: LoanRepository
) {
    operator fun invoke(accountId: String): Flow<List<Loan>> =
        repository.getActiveByAccount(accountId)
}
