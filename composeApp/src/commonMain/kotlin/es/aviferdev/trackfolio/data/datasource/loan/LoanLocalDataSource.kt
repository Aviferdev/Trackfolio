package es.aviferdev.trackfolio.data.datasource.loan

import es.aviferdev.trackfolio.domain.model.Loan
import kotlinx.coroutines.flow.Flow

interface LoanLocalDataSource {
    fun getByAccount(accountId: String): Flow<List<Loan>>
    fun getActiveByAccount(accountId: String): Flow<List<Loan>>
    fun getById(id: String): Flow<Loan?>
    fun getAll(): Flow<List<Loan>>
    fun getTotalOutstandingByAccount(accountId: String): Flow<Double>
    suspend fun insert(loan: Loan): Result<Unit>
    suspend fun update(loan: Loan): Result<Unit>
    suspend fun updateRate(id: String, newRate: Double, newMonthlyPayment: Double, outstandingPrincipal: Double): Result<Unit>
    suspend fun incrementPaidInstallments(id: String, newOutstanding: Double): Result<Unit>
    suspend fun archive(id: String): Result<Unit>
}
