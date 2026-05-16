package es.aviferdev.n3to.data.datasource.platform

import es.aviferdev.n3to.domain.model.Platform
import kotlinx.coroutines.flow.Flow

interface PlatformLocalDataSource {
    fun getAll(): Flow<List<Platform>>
    fun getAllIncludingArchived(): Flow<List<Platform>>
    fun getById(id: String): Flow<Platform?>
    fun count(): Flow<Long>

    suspend fun insert(platform: Platform): Result<Unit>
    suspend fun rename(id: String, newName: String, newIcon: String, notes: String?): Result<Unit>
    suspend fun updateSortOrder(id: String, sortOrder: Int): Result<Unit>
    suspend fun archive(id: String): Result<Unit>
    suspend fun unarchive(id: String): Result<Unit>
}
