package es.aviferdev.n3to.domain.usecase.loan

import es.aviferdev.n3to.domain.model.Loan
import es.aviferdev.n3to.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow

class GetLoanByIdUseCase(private val repository: LoanRepository) {
    operator fun invoke(loanId: String): Flow<Loan?> = repository.getById(loanId)
}
