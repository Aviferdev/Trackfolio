package es.aviferdev.n3to.data.datasource.loan

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.n3to.data.database.N3toDatabase
import es.aviferdev.n3to.data.database.mapper.toDomain
import es.aviferdev.n3to.data.database.mapper.toEntity
import es.aviferdev.n3to.domain.model.Loan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LoanLocalDataSourceImpl(
    private val database: N3toDatabase
) : LoanLocalDataSource {

    private val queries = database.loanQueries

    override fun getByAccount(accountId: String): Flow<List<Loan>> =
        queries.selectByAccount(accountId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getActiveByAccount(accountId: String): Flow<List<Loan>> =
        queries.selectActiveByAccount(accountId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getById(id: String): Flow<Loan?> =
        queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override fun getAll(): Flow<List<Loan>> =
        queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getTotalOutstandingByAccount(accountId: String): Flow<Double> =
        queries.getTotalOutstandingByAccount(accountId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { row -> row ?: 0.0 }

    override suspend fun insert(loan: Loan): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                val e = loan.toEntity()
                queries.insert(
                    id = e.id,
                    accountId = e.accountId,
                    name = e.name,
                    type = e.type,
                    totalAmount = e.totalAmount,
                    outstandingPrincipal = e.outstandingPrincipal,
                    currentInterestRate = e.currentInterestRate,
                    monthlyPayment = e.monthlyPayment,
                    totalInstallments = e.totalInstallments,
                    paidInstallments = e.paidInstallments,
                    startDate = e.startDate,
                    endDate = e.endDate,
                    lenderName = e.lenderName,
                    notes = e.notes,
                    createdAt = e.createdAt
                )
            }
        }

    override suspend fun update(loan: Loan): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                val e = loan.toEntity()
                queries.update(
                    id = e.id,
                    name = e.name,
                    type = e.type,
                    totalAmount = e.totalAmount,
                    outstandingPrincipal = e.outstandingPrincipal,
                    currentInterestRate = e.currentInterestRate,
                    monthlyPayment = e.monthlyPayment,
                    totalInstallments = e.totalInstallments,
                    paidInstallments = e.paidInstallments,
                    startDate = e.startDate,
                    endDate = e.endDate,
                    lenderName = e.lenderName,
                    notes = e.notes
                )
            }
        }

    override suspend fun updateRate(
        id: String,
        newRate: Double,
        newMonthlyPayment: Double,
        outstandingPrincipal: Double
    ): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.updateRate(
                    id = id,
                    newRate = newRate,
                    newMonthlyPayment = newMonthlyPayment,
                    outstandingPrincipal = outstandingPrincipal
                )
            }
        }

    override suspend fun incrementPaidInstallments(
        id: String,
        newOutstanding: Double
    ): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.incrementPaidInstallments(
                    id = id,
                    newOutstanding = newOutstanding
                )
            }
        }

    override suspend fun close(id: String, closedAt: Long, closeType: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.close(
                    closedAt = closedAt,
                    closeType = closeType,
                    id = id
                )
            }
        }

    override suspend fun archive(id: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) { queries.archive(id) }
        }

    override suspend fun unarchive(id: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) { queries.unarchive(id) }
        }
}
