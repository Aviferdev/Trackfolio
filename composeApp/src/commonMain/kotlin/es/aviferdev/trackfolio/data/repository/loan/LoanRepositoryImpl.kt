package es.aviferdev.trackfolio.data.repository.loan

import es.aviferdev.trackfolio.data.datasource.loan.LoanLocalDataSource
import es.aviferdev.trackfolio.domain.model.Loan
import es.aviferdev.trackfolio.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow

class LoanRepositoryImpl(
    private val localDataSource: LoanLocalDataSource
) : LoanRepository {

    override fun getByAccount(accountId: String): Flow<List<Loan>> =
        localDataSource.getByAccount(accountId)

    override fun getActiveByAccount(accountId: String): Flow<List<Loan>> =
        localDataSource.getActiveByAccount(accountId)

    override fun getById(id: String): Flow<Loan?> =
        localDataSource.getById(id)

    override fun getAll(): Flow<List<Loan>> =
        localDataSource.getAll()

    override fun getTotalOutstandingByAccount(accountId: String): Flow<Double> =
        localDataSource.getTotalOutstandingByAccount(accountId)

    override suspend fun insert(loan: Loan): Result<Unit> =
        localDataSource.insert(loan)

    override suspend fun update(loan: Loan): Result<Unit> =
        localDataSource.update(loan)

    override suspend fun updateRate(
        id: String,
        newRate: Double,
        newMonthlyPayment: Double,
        outstandingPrincipal: Double
    ): Result<Unit> =
        localDataSource.updateRate(id, newRate, newMonthlyPayment, outstandingPrincipal)

    override suspend fun incrementPaidInstallments(id: String, newOutstanding: Double): Result<Unit> =
        localDataSource.incrementPaidInstallments(id, newOutstanding)

    override suspend fun archive(id: String): Result<Unit> =
        localDataSource.archive(id)
}
