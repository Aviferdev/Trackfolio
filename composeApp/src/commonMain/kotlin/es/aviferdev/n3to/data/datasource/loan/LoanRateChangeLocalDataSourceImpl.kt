package es.aviferdev.n3to.data.datasource.loan

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import es.aviferdev.n3to.data.database.N3toDatabase
import es.aviferdev.n3to.data.database.mapper.toDomain
import es.aviferdev.n3to.data.database.mapper.toEntity
import es.aviferdev.n3to.domain.model.LoanRateChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LoanRateChangeLocalDataSourceImpl(
    private val database: N3toDatabase
) : LoanRateChangeLocalDataSource {

    private val queries = database.loanRateChangeQueries

    override fun getByLoan(loanId: String): Flow<List<LoanRateChange>> =
        queries.selectByLoan(loanId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun insert(rateChange: LoanRateChange): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                val e = rateChange.toEntity()
                queries.insert(
                    id            = e.id,
                    loanId        = e.loanId,
                    newRate       = e.newRate,
                    previousRate  = e.previousRate,
                    effectiveDate = e.effectiveDate,
                    createdAt     = e.createdAt
                )
            }
        }

    override suspend fun deleteByLoan(loanId: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) { queries.deleteByLoan(loanId) }
        }
}
