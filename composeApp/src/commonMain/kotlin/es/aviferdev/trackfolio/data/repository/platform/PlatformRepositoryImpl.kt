package es.aviferdev.trackfolio.data.repository.platform

import es.aviferdev.trackfolio.data.datasource.platform.PlatformLocalDataSource
import es.aviferdev.trackfolio.domain.model.Platform
import es.aviferdev.trackfolio.domain.repository.PlatformRepository
import kotlinx.coroutines.flow.Flow

class PlatformRepositoryImpl(
    private val dataSource: PlatformLocalDataSource
) : PlatformRepository {

    override fun getAll(): Flow<List<Platform>> = dataSource.getAll()
    override fun getAllIncludingArchived(): Flow<List<Platform>> = dataSource.getAllIncludingArchived()
    override fun getById(id: String): Flow<Platform?> = dataSource.getById(id)
    override fun count(): Flow<Long> = dataSource.count()

    override suspend fun save(platform: Platform): Result<Unit> = dataSource.insert(platform)

    override suspend fun rename(id: String, newName: String, newIcon: String, notes: String?): Result<Unit> =
        dataSource.rename(id, newName, newIcon, notes)

    override suspend fun reorder(id: String, sortOrder: Int): Result<Unit> =
        dataSource.updateSortOrder(id, sortOrder)

    override suspend fun archive(id: String): Result<Unit> = dataSource.archive(id)
    override suspend fun unarchive(id: String): Result<Unit> = dataSource.unarchive(id)
}