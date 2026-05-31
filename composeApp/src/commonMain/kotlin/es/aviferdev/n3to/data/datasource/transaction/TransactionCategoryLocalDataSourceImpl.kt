package es.aviferdev.n3to.data.datasource.transaction

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.n3to.data.database.N3toDatabase
import es.aviferdev.n3to.data.database.mapper.toDomain
import es.aviferdev.n3to.data.database.mapper.toEntity
import es.aviferdev.n3to.domain.model.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TransactionCategoryLocalDataSourceImpl(
    private val database: N3toDatabase
) : TransactionCategoryLocalDataSource {

    private val queries = database.categoryQueries

    override fun getByAccount(accountId: String): Flow<List<Category>> =
        queries.selectAll(accountId).asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getByTypeAndAccount(accountId: String, type: String): Flow<List<Category>> =
        queries.selectByType(accountId, type).asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getAllIncludingArchivedByAccount(accountId: String): Flow<List<Category>> =
        queries.selectAllIncludingArchived(accountId).asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getByTypeIncludingArchivedByAccount(
        accountId: String,
        type: String
    ): Flow<List<Category>> =
        queries.selectByTypeIncludingArchived(accountId, type).asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getById(id: String): Flow<Category?> =
        queries.selectById(id).asFlow().mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override fun countByAccount(accountId: String): Flow<Long> =
        queries.countAll(accountId).asFlow().mapToOne(Dispatchers.IO)

    override suspend fun insert(entity: Category): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                val e = entity.toEntity()
                queries.insert(
                    id = e.id,
                    accountId = e.accountId,
                    name = e.name,
                    type = e.type,
                    isDefault = e.isDefault,
                    createdAt = e.createdAt
                )
            }
        }

    override suspend fun updateName(id: String, name: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.updateName(name = name, id = id)
            }
        }

    override suspend fun archive(id: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.archive(id)
            }
        }

    override suspend fun unarchive(id: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.unarchive(id)
            }
        }
}
