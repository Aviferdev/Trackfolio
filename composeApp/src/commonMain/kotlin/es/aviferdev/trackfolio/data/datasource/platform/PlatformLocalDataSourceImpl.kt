package es.aviferdev.trackfolio.data.datasource.platform

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.trackfolio.data.database.TrackfolioDatabase
import es.aviferdev.trackfolio.data.database.mapper.toDomain
import es.aviferdev.trackfolio.domain.model.Platform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PlatformLocalDataSourceImpl(
    private val database: TrackfolioDatabase
) : PlatformLocalDataSource {

    private val queries = database.platformQueries

    override fun getAll(): Flow<List<Platform>> =
        queries.selectAll().asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getAllIncludingArchived(): Flow<List<Platform>> =
        queries.selectAllIncludingArchived().asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getById(id: String): Flow<Platform?> =
        queries.selectById(id).asFlow().mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override fun count(): Flow<Long> =
        queries.countAll().asFlow().mapToOne(Dispatchers.IO)

    override suspend fun insert(platform: Platform): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.insert(
                id        = platform.id,
                name      = platform.name,
                icon      = platform.icon,
                sortOrder = platform.sortOrder.toLong(),
                createdAt = platform.createdAt,
                notes     = platform.notes
            )
        }
    }

    override suspend fun rename(id: String, newName: String, newIcon: String, notes: String?): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.updateNameAndIcon(name = newName, icon = newIcon, id = id)
            if (notes != null) {
                queries.updateNotes(notes = notes, id = id)
            }
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
