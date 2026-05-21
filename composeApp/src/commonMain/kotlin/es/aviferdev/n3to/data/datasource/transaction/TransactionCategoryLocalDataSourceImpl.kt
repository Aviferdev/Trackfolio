package es.aviferdev.n3to.data.datasource.transaction

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.n3to.data.database.CategoryEntity
import es.aviferdev.n3to.data.database.N3toDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TransactionCategoryLocalDataSourceImpl(
    private val database: N3toDatabase
) : TransactionCategoryLocalDataSource {

    private val queries = database.categoryQueries

    override fun getByAccount(accountId: String): Flow<List<CategoryEntity>> =
        queries.selectAll(accountId).asFlow().mapToList(Dispatchers.IO)

    override fun getByTypeAndAccount(accountId: String, type: String): Flow<List<CategoryEntity>> =
        queries.selectByType(accountId, type).asFlow().mapToList(Dispatchers.IO)

    override fun getAllIncludingArchivedByAccount(accountId: String): Flow<List<CategoryEntity>> =
        queries.selectAllIncludingArchived(accountId).asFlow().mapToList(Dispatchers.IO)

    override fun getByTypeIncludingArchivedByAccount(
        accountId: String,
        type: String
    ): Flow<List<CategoryEntity>> =
        queries.selectByTypeIncludingArchived(accountId, type).asFlow().mapToList(Dispatchers.IO)

    override fun getById(id: String): Flow<CategoryEntity?> =
        queries.selectById(id).asFlow().mapToOneOrNull(Dispatchers.IO)

    override fun countByAccount(accountId: String): Flow<Long> =
        queries.countAll(accountId).asFlow().mapToOne(Dispatchers.IO)

    override suspend fun insert(entity: CategoryEntity): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.insert(
                    id = entity.id,
                    accountId = entity.accountId,
                    name = entity.name,
                    type = entity.type,
                    isDefault = entity.isDefault
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
