package es.aviferdev.trackfolio.data.repository

import es.aviferdev.trackfolio.data.database.mapper.toDomain
import es.aviferdev.trackfolio.data.database.mapper.toEntity
import es.aviferdev.trackfolio.data.datasource.CategoryLocalDataSource
import es.aviferdev.trackfolio.domain.model.Category
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val dataSource: CategoryLocalDataSource
) : CategoryRepository {

    override fun getAll(): Flow<List<Category>> =
        dataSource.getAll().map { it.map { entity -> entity.toDomain() } }

    override fun getByType(type: TransactionType): Flow<List<Category>> =
        dataSource.getByType(type.name).map { it.map { entity -> entity.toDomain() } }

    override fun count(): Flow<Long> =
        dataSource.count()

    override suspend fun save(category: Category): Result<Unit> =
        dataSource.insert(category.toEntity())

    override suspend fun delete(id: String): Result<Unit> =
        dataSource.delete(id)
}
