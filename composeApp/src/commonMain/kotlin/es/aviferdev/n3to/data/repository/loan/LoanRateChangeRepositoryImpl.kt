package es.aviferdev.n3to.data.repository.loan

import es.aviferdev.n3to.data.datasource.loan.LoanLocalDataSource
import es.aviferdev.n3to.domain.model.LoanRateChange
import es.aviferdev.n3to.domain.repository.LoanRateChangeRepository
import kotlinx.coroutines.flow.Flow

class LoanRateChangeRepositoryImpl(
    private val loanDataSource: LoanLocalDataSource
) : LoanRateChangeRepository {

    override fun getByLoan(loanId: String): Flow<List<LoanRateChange>> =
        loanDataSource.getRateChangesByLoan(loanId)

    override suspend fun insert(rateChange: LoanRateChange): Result<Unit> =
        loanDataSource.insertRateChange(rateChange)

    override suspend fun deleteByLoan(loanId: String): Result<Unit> =
        loanDataSource.deleteRateChangesByLoan(loanId)
}
