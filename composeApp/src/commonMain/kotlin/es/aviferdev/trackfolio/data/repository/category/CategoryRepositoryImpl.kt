package es.aviferdev.trackfolio.data.repository.category

import es.aviferdev.trackfolio.data.database.mapper.toDomain
import es.aviferdev.trackfolio.data.database.mapper.toEntity
import es.aviferdev.trackfolio.data.datasource.transaction.TransactionCategoryLocalDataSource
import es.aviferdev.trackfolio.domain.model.Category
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val dataSource: TransactionCategoryLocalDataSource
) : CategoryRepository {

    override fun getAll(): Flow<List<Category>> =
        dataSource.getAll().map { list -> list.map { it.toDomain() } }

    override fun getByType(type: TransactionType): Flow<List<Category>> =
        dataSource.getByType(type.name).map { list -> list.map { it.toDomain() } }

    override fun getAllIncludingArchived(): Flow<List<Category>> =
        dataSource.getAllIncludingArchived().map { list -> list.map { it.toDomain() } }

    override fun count(): Flow<Long> =
        dataSource.count()

    override suspend fun save(category: Category): Result<Unit> =
        dataSource.insert(category.toEntity())

    override suspend fun rename(id: String, newName: String): Result<Unit> =
        dataSource.updateName(id, newName)

    override suspend fun archive(id: String): Result<Unit> =
        dataSource.archive(id)

    override suspend fun unarchive(id: String): Result<Unit> =
        dataSource.unarchive(id)
}