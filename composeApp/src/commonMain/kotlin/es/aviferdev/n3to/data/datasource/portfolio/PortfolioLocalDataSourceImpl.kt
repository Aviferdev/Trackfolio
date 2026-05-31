package es.aviferdev.n3to.data.datasource.portfolio

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.n3to.data.database.N3toDatabase
import es.aviferdev.n3to.data.database.mapper.toDomain
import es.aviferdev.n3to.data.database.mapper.toEntity
import es.aviferdev.n3to.domain.model.Portfolio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PortfolioLocalDataSourceImpl(
    private val database: N3toDatabase
) : PortfolioLocalDataSource {

    private val queries = database.portfolioQueries

    override fun getByAccount(accountId: String): Flow<List<Portfolio>> =
        queries.selectByAccount(accountId).asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getArchivedByAccount(accountId: String): Flow<List<Portfolio>> =
        queries.selectArchivedByAccount(accountId).asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getById(id: String): Flow<Portfolio?> =
        queries.selectById(id).asFlow().mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override fun countByAccount(accountId: String): Flow<Long> =
        queries.countByAccount(accountId).asFlow().mapToOne(Dispatchers.IO)

    override suspend fun insert(entity: Portfolio) {
        withContext(Dispatchers.IO) {
            val e = entity.toEntity()
            queries.insert(
                id = e.id,
                accountId = e.accountId,
                name = e.name,
                description = e.description,
                color = e.color,
                sortOrder = e.sortOrder,
                createdAt = e.createdAt,
                archived = e.archived
            )
        }
    }

    override suspend fun update(entity: Portfolio) {
        withContext(Dispatchers.IO) {
            val e = entity.toEntity()
            queries.update(
                name = e.name,
                description = e.description,
                color = e.color,
                sortOrder = e.sortOrder,
                id = e.id
            )
        }
    }

    override suspend fun archive(id: String) {
        withContext(Dispatchers.IO) {
            queries.archive(id)
        }
    }

    override suspend fun unarchive(id: String) {
        withContext(Dispatchers.IO) {
            queries.unarchive(id)
        }
    }

    override suspend fun delete(id: String) {
        withContext(Dispatchers.IO) {
            queries.delete(id)
        }
    }
}
