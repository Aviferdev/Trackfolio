package es.aviferdev.n3to.data.repository.loan

import es.aviferdev.n3to.data.datasource.loan.LoanRateChangeLocalDataSource
import es.aviferdev.n3to.domain.model.LoanRateChange
import es.aviferdev.n3to.domain.repository.LoanRateChangeRepository
import kotlinx.coroutines.flow.Flow

class LoanRateChangeRepositoryImpl(
    private val localDataSource: LoanRateChangeLocalDataSource
) : LoanRateChangeRepository {

    override fun getByLoan(loanId: String): Flow<List<LoanRateChange>> =
        localDataSource.getByLoan(loanId)

    override suspend fun insert(rateChange: LoanRateChange): Result<Unit> =
        localDataSource.insert(rateChange)

    override suspend fun deleteByLoan(loanId: String): Result<Unit> =
        localDataSource.deleteByLoan(loanId)
}
