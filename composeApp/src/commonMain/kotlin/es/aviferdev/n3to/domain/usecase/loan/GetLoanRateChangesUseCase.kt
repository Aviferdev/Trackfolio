package es.aviferdev.n3to.domain.usecase.loan

import es.aviferdev.n3to.domain.model.LoanRateChange
import es.aviferdev.n3to.domain.repository.LoanRateChangeRepository
import kotlinx.coroutines.flow.Flow

class GetLoanRateChangesUseCase(private val repository: LoanRateChangeRepository) {
    operator fun invoke(loanId: String): Flow<List<LoanRateChange>> = repository.getByLoan(loanId)
}
