package es.aviferdev.n3to.data.datasource.valuable

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.n3to.data.database.N3toDatabase
import es.aviferdev.n3to.data.database.mapper.toDomain
import es.aviferdev.n3to.data.database.mapper.toEntity
import es.aviferdev.n3to.domain.model.Valuable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ValuableLocalDataSourceImpl(
    private val database: N3toDatabase
) : ValuableLocalDataSource {

    private val queries = database.valuableQueries

    override fun getByAccount(accountId: String): Flow<List<Valuable>> =
        queries.selectByAccount(accountId)
            .asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getActiveByAccount(accountId: String): Flow<List<Valuable>> =
        queries.selectActiveByAccount(accountId)
            .asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getById(id: String): Flow<Valuable?> =
        queries.selectById(id)
            .asFlow().mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override suspend fun insert(valuable: Valuable): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val e = valuable.toEntity()
            queries.insert(
                id = e.id,
                accountId = e.accountId,
                name = e.name,
                description = e.description,
                purchasePrice = e.purchasePrice,
                purchaseDate = e.purchaseDate,
                estimatedValue = e.estimatedValue,
                salePrice = e.salePrice,
                saleDate = e.saleDate,
                closeType = e.closeType,
                linkedLoanId = e.linkedLoanId,
                notes = e.notes,
                archived = e.archived,
                createdAt = e.createdAt
            )
        }
    }

    override suspend fun sell(id: String, saleDate: Long, salePrice: Double, closeType: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.sell(
                    saleDate = saleDate,
                    salePrice = salePrice,
                    closeType = closeType,
                    id = id
                )
            }
        }

    override suspend fun updateEstimatedValue(id: String, value: Double): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.updateEstimatedValue(
                    value = value,
                    id = id
                )
            }
        }

    override suspend fun updateLinkedLoan(valuableId: String, loanId: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.updateLinkedLoan(
                    loanId = loanId,
                    id = valuableId
                )
            }
        }

    override suspend fun archive(id: String): Result<Unit> =
        runCatching { withContext(Dispatchers.IO) { queries.archive(id) } }

    override suspend fun unarchive(id: String): Result<Unit> =
        runCatching { withContext(Dispatchers.IO) { queries.unarchive(id) } }

    override suspend fun delete(id: String): Result<Unit> =
        runCatching { withContext(Dispatchers.IO) { queries.deleteById(id) } }
}
