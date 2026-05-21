package es.aviferdev.n3to.data.datasource.portfolio

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.n3to.data.database.N3toDatabase
import es.aviferdev.n3to.data.database.PortfolioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PortfolioLocalDataSourceImpl(
    private val database: N3toDatabase
) : PortfolioLocalDataSource {

    private val queries = database.portfolioQueries

    override fun getByAccount(accountId: String): Flow<List<PortfolioEntity>> =
        queries.selectByAccount(accountId).asFlow().mapToList(Dispatchers.IO)

    override fun getById(id: String): Flow<PortfolioEntity?> =
        queries.selectById(id).asFlow().mapToOneOrNull(Dispatchers.IO)

    override fun countByAccount(accountId: String): Flow<Long> =
        queries.countByAccount(accountId).asFlow().mapToOne(Dispatchers.IO)

    override suspend fun insert(entity: PortfolioEntity) {
        withContext(Dispatchers.IO) {
            queries.insert(
                id = entity.id,
                accountId = entity.accountId,
                name = entity.name,
                description = entity.description,
                color = entity.color,
                sortOrder = entity.sortOrder,
                createdAt = entity.createdAt
            )
        }
    }

    override suspend fun update(entity: PortfolioEntity) {
        withContext(Dispatchers.IO) {
            queries.update(
                name = entity.name,
                description = entity.description,
                color = entity.color,
                sortOrder = entity.sortOrder,
                id = entity.id
            )
        }
    }

    override suspend fun delete(id: String) {
        withContext(Dispatchers.IO) {
            queries.delete(id)
        }
    }
}
