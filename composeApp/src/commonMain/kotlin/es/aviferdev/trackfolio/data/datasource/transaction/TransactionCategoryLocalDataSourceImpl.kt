package es.aviferdev.trackfolio.data.datasource.transaction

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.trackfolio.data.database.CategoryEntity
import es.aviferdev.trackfolio.data.database.TrackfolioDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TransactionCategoryLocalDataSourceImpl(
    private val database: TrackfolioDatabase
) : TransactionCategoryLocalDataSource {

    private val queries = database.categoryQueries

    override fun getAll(): Flow<List<CategoryEntity>> =
        queries.selectAll().asFlow().mapToList(Dispatchers.IO)

    override fun getByType(type: String): Flow<List<CategoryEntity>> =
        queries.selectByType(type).asFlow().mapToList(Dispatchers.IO)

    override fun getAllIncludingArchived(): Flow<List<CategoryEntity>> =
        queries.selectAllIncludingArchived().asFlow().mapToList(Dispatchers.IO)

    override fun getById(id: String): Flow<CategoryEntity?> =
        queries.selectById(id).asFlow().mapToOneOrNull(Dispatchers.IO)

    override fun count(): Flow<Long> =
        queries.countAll().asFlow().mapToOne(Dispatchers.IO)

    override suspend fun insert(entity: CategoryEntity): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.insert(
                    id        = entity.id,
                    name      = entity.name,
                    type      = entity.type,
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
