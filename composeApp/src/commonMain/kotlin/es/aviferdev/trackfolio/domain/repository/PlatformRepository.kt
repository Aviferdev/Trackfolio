package es.aviferdev.trackfolio.domain.repository

import es.aviferdev.trackfolio.domain.model.Platform
import kotlinx.coroutines.flow.Flow

interface PlatformRepository {
    fun getAll(): Flow<List<Platform>>
    fun getAllIncludingArchived(): Flow<List<Platform>>
    fun getById(id: String): Flow<Platform?>
    fun count(): Flow<Long>

    suspend fun save(platform: Platform): Result<Unit>
    suspend fun rename(id: String, newName: String, newIcon: String, notes: String?): Result<Unit>
    suspend fun reorder(id: String, sortOrder: Int): Result<Unit>
    suspend fun archive(id: String): Result<Unit>
    suspend fun unarchive(id: String): Result<Unit>
}
