package es.aviferdev.n3to.data.datasource.asset

import es.aviferdev.n3to.domain.model.AssetCategory
import kotlinx.coroutines.flow.Flow

interface AssetCategoryLocalDataSource {
    fun getAll(): Flow<List<AssetCategory>>
    fun getAllIncludingArchived(): Flow<List<AssetCategory>>
    fun getById(id: String): Flow<AssetCategory?>
    fun count(): Flow<Long>

    suspend fun insert(category: AssetCategory): Result<Unit>
    suspend fun rename(id: String, newName: String, newIcon: String): Result<Unit>
    suspend fun updateSortOrder(id: String, sortOrder: Int): Result<Unit>
    suspend fun archive(id: String): Result<Unit>
    suspend fun unarchive(id: String): Result<Unit>
}
