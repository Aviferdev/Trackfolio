package es.aviferdev.trackfolio.data.datasource.asset

import es.aviferdev.trackfolio.domain.model.AssetTag
import es.aviferdev.trackfolio.domain.model.AssetTagAssignment
import kotlinx.coroutines.flow.Flow

interface AssetTagLocalDataSource {
    // ── Tags ────────────────────────────────────────────────────────────────
    fun getAllTags(): Flow<List<AssetTag>>
    fun getTagsByCategory(categoryId: String): Flow<List<AssetTag>>
    fun getGlobalTags(): Flow<List<AssetTag>>
    fun getTagById(id: String): Flow<AssetTag?>

    suspend fun insertTag(tag: AssetTag): Result<Unit>
    suspend fun renameTag(id: String, newName: String, newColor: String): Result<Unit>
    suspend fun archiveTag(id: String): Result<Unit>
    suspend fun unarchiveTag(id: String): Result<Unit>

    // ── Asignaciones tag ↔ asset ─────────────────────────────────────────────
    fun getAssignmentsForAsset(assetId: String): Flow<List<AssetTagAssignment>>

    suspend fun upsertAssignment(assetId: String, tagId: String, weight: Double): Result<Unit>
    suspend fun removeAssignment(assetId: String, tagId: String): Result<Unit>
    suspend fun removeAllAssignmentsForAsset(assetId: String): Result<Unit>
}
