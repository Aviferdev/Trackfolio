package es.aviferdev.trackfolio.domain.repository

import es.aviferdev.trackfolio.domain.model.LoanRateChange
import kotlinx.coroutines.flow.Flow

interface LoanRateChangeRepository {
    fun getByLoan(loanId: String): Flow<List<LoanRateChange>>
    suspend fun insert(rateChange: LoanRateChange): Result<Unit>
    suspend fun deleteByLoan(loanId: String): Result<Unit>
}
