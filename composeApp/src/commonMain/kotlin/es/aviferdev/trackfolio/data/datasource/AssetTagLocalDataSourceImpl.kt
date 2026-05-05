package es.aviferdev.trackfolio.data.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.trackfolio.data.database.TrackfolioDatabase
import es.aviferdev.trackfolio.data.database.mapper.toDomain
import es.aviferdev.trackfolio.domain.model.AssetTag
import es.aviferdev.trackfolio.domain.model.AssetTagAssignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AssetTagLocalDataSourceImpl(
    private val database: TrackfolioDatabase
) : AssetTagLocalDataSource {

    private val queries = database.assetTagQueries

    // ── Tags ─────────────────────────────────────────────────────────────────

    override fun getAllTags(): Flow<List<AssetTag>> =
        queries.selectAll().asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getTagsByCategory(categoryId: String): Flow<List<AssetTag>> =
        queries.selectByCategory(categoryId).asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getGlobalTags(): Flow<List<AssetTag>> =
        queries.selectGlobal().asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getTagById(id: String): Flow<AssetTag?> =
        queries.selectById(id).asFlow().mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override suspend fun insertTag(tag: AssetTag): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.insertTag(
                id         = tag.id,
                name       = tag.name,
                categoryId = tag.categoryId,
                color      = tag.color,
                createdAt  = tag.createdAt
            )
        }
    }

    override suspend fun renameTag(id: String, newName: String, newColor: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.updateTagName(name = newName, color = newColor, id = id)
        }
    }

    override suspend fun archiveTag(id: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) { queries.archiveTag(id) }
    }

    override suspend fun unarchiveTag(id: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) { queries.unarchiveTag(id) }
    }

    // ── Asignaciones ─────────────────────────────────────────────────────────

    override fun getAssignmentsForAsset(assetId: String): Flow<List<AssetTagAssignment>> =
        queries.selectAssignmentsByAsset(assetId).asFlow().mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    AssetTagAssignment(
                        assetId = assetId,
                        tag     = AssetTag(
                            id         = row.id,
                            name       = row.name,
                            categoryId = row.categoryId,
                            color      = row.color,
                            archived   = row.archived != 0L,
                            createdAt  = row.createdAt
                        ),
                        weight  = row.weight
                    )
                }
            }

    override suspend fun upsertAssignment(assetId: String, tagId: String, weight: Double): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.upsertAssignment(assetId = assetId, tagId = tagId, weight = weight)
        }
    }

    override suspend fun removeAssignment(assetId: String, tagId: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.deleteAssignment(assetId = assetId, tagId = tagId)
        }
    }

    override suspend fun removeAllAssignmentsForAsset(assetId: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.deleteAssignmentsByAsset(assetId)
        }
    }
}
