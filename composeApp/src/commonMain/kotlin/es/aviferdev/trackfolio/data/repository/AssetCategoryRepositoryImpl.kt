package es.aviferdev.trackfolio.data.repository

import es.aviferdev.trackfolio.data.datasource.AssetCategoryLocalDataSource
import es.aviferdev.trackfolio.domain.model.AssetCategory
import es.aviferdev.trackfolio.domain.repository.AssetCategoryRepository
import kotlinx.coroutines.flow.Flow

class AssetCategoryRepositoryImpl(
    private val dataSource: AssetCategoryLocalDataSource
) : AssetCategoryRepository {

    override fun getAll(): Flow<List<AssetCategory>> = dataSource.getAll()
    override fun getAllIncludingArchived(): Flow<List<AssetCategory>> = dataSource.getAllIncludingArchived()
    override fun getById(id: String): Flow<AssetCategory?> = dataSource.getById(id)
    override fun count(): Flow<Long> = dataSource.count()

    override suspend fun save(category: AssetCategory): Result<Unit> = dataSource.insert(category)
    override suspend fun rename(id: String, newName: String, newIcon: String): Result<Unit> =
        dataSource.rename(id, newName, newIcon)

    override suspend fun reorder(id: String, sortOrder: Int): Result<Unit> =
        dataSource.updateSortOrder(id, sortOrder)

    override suspend fun archive(id: String): Result<Unit> = dataSource.archive(id)
    override suspend fun unarchive(id: String): Result<Unit> = dataSource.unarchive(id)
}
