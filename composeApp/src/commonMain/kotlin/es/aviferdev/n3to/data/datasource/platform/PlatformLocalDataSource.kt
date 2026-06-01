package es.aviferdev.n3to.data.datasource.platform

import es.aviferdev.n3to.domain.model.Platform
import kotlinx.coroutines.flow.Flow

interface PlatformLocalDataSource {
    // ── Platform ──────────────────────────────────────────────────────────────
    fun getAll(): Flow<List<Platform>>
    fun getAllIncludingArchived(): Flow<List<Platform>>
    fun getById(id: String): Flow<Platform?>
    fun count(): Flow<Long>

    suspend fun insert(platform: Platform): Result<Unit>
    suspend fun rename(id: String, newName: String, newIcon: String, notes: String?): Result<Unit>
    suspend fun updateSortOrder(id: String, sortOrder: Int): Result<Unit>
    suspend fun archive(id: String): Result<Unit>
    suspend fun unarchive(id: String): Result<Unit>

    // ── PlatformCategory (fusionado) ──────────────────────────────────────────
    fun getPlatformsByCategory(assetCategoryId: String): Flow<List<Platform>>
    fun getPlatformsByCategoryIncludingArchived(assetCategoryId: String): Flow<List<Platform>>
    fun getCategoriesByPlatform(platformId: String): Flow<List<String>>
    suspend fun linkPlatformToCategory(platformId: String, assetCategoryId: String): Result<Unit>
    suspend fun unlinkPlatformFromCategory(platformId: String, assetCategoryId: String): Result<Unit>
    suspend fun unlinkAllPlatformsByCategory(assetCategoryId: String): Result<Unit>
}
