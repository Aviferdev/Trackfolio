package es.aviferdev.n3to.data.datasource.loan

import es.aviferdev.n3to.domain.model.LoanRateChange
import kotlinx.coroutines.flow.Flow

interface LoanRateChangeLocalDataSource {
    fun getByLoan(loanId: String): Flow<List<LoanRateChange>>
    suspend fun insert(rateChange: LoanRateChange): Result<Unit>
    suspend fun deleteByLoan(loanId: String): Result<Unit>
}
