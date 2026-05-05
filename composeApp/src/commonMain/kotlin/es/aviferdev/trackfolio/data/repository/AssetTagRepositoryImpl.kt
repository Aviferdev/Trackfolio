package es.aviferdev.trackfolio.data.repository

import es.aviferdev.trackfolio.data.datasource.AssetTagLocalDataSource
import es.aviferdev.trackfolio.domain.model.AssetTag
import es.aviferdev.trackfolio.domain.model.AssetTagAssignment
import es.aviferdev.trackfolio.domain.repository.AssetTagRepository
import kotlinx.coroutines.flow.Flow

class AssetTagRepositoryImpl(
    private val dataSource: AssetTagLocalDataSource
) : AssetTagRepository {

    override fun getAllTags(): Flow<List<AssetTag>> = dataSource.getAllTags()
    override fun getTagsByCategory(categoryId: String): Flow<List<AssetTag>> = dataSource.getTagsByCategory(categoryId)
    override fun getGlobalTags(): Flow<List<AssetTag>> = dataSource.getGlobalTags()
    override fun getTagById(id: String): Flow<AssetTag?> = dataSource.getTagById(id)

    override suspend fun saveTag(tag: AssetTag): Result<Unit> = dataSource.insertTag(tag)
    override suspend fun renameTag(id: String, newName: String, newColor: String): Result<Unit> =
        dataSource.renameTag(id, newName, newColor)
    override suspend fun archiveTag(id: String): Result<Unit> = dataSource.archiveTag(id)
    override suspend fun unarchiveTag(id: String): Result<Unit> = dataSource.unarchiveTag(id)

    override fun getAssignmentsForAsset(assetId: String): Flow<List<AssetTagAssignment>> =
        dataSource.getAssignmentsForAsset(assetId)

    override suspend fun upsertAssignment(assetId: String, tagId: String, weight: Double): Result<Unit> =
        dataSource.upsertAssignment(assetId, tagId, weight)

    override suspend fun removeAssignment(assetId: String, tagId: String): Result<Unit> =
        dataSource.removeAssignment(assetId, tagId)

    override suspend fun removeAllAssignmentsForAsset(assetId: String): Result<Unit> =
        dataSource.removeAllAssignmentsForAsset(assetId)
}
