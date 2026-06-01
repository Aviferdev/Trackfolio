package es.aviferdev.n3to.data.datasource.asset

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.n3to.data.database.N3toDatabase
import es.aviferdev.n3to.data.database.mapper.toDomain
import es.aviferdev.n3to.domain.model.AssetCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AssetCategoryLocalDataSourceImpl(
    private val database: N3toDatabase
) : AssetCategoryLocalDataSource {

    private val queries = database.assetCategoryQueries

    override fun getAll(): Flow<List<AssetCategory>> =
        queries.selectAll().asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getAllIncludingArchived(): Flow<List<AssetCategory>> =
        queries.selectAllIncludingArchived().asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getById(id: String): Flow<AssetCategory?> =
        queries.selectById(id).asFlow().mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override fun count(): Flow<Long> =
        queries.countAll().asFlow().mapToOne(Dispatchers.IO)

    override suspend fun insert(category: AssetCategory): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.insert(
                id = category.id,
                name = category.name,
                icon = category.icon,
                sortOrder = category.sortOrder.toLong(),
                isQuotable = if (category.isQuotable) 1L else 0L,
                createdAt = category.createdAt
            )
        }
    }

    override suspend fun rename(id: String, newName: String, newIcon: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.updateNameAndIcon(name = newName, icon = newIcon, id = id)
            }
        }

    override suspend fun updateSortOrder(id: String, sortOrder: Int): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.updateSortOrder(sortOrder = sortOrder.toLong(), id = id)
        }
    }

    override suspend fun archive(id: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) { queries.archive(id) }
    }

    override suspend fun unarchive(id: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) { queries.unarchive(id) }
    }
}
