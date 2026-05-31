package es.aviferdev.n3to.data.repository.category

import es.aviferdev.n3to.data.datasource.transaction.TransactionCategoryLocalDataSource
import es.aviferdev.n3to.domain.model.Category
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class CategoryRepositoryImpl(
    private val dataSource: TransactionCategoryLocalDataSource
) : CategoryRepository {

    override fun getByAccount(accountId: String): Flow<List<Category>> =
        dataSource.getByAccount(accountId)

    override fun getByTypeAndAccount(
        accountId: String,
        type: TransactionType
    ): Flow<List<Category>> =
        dataSource.getByTypeAndAccount(accountId, type.name)

    override fun getAllIncludingArchivedByAccount(accountId: String): Flow<List<Category>> =
        dataSource.getAllIncludingArchivedByAccount(accountId)

    override fun countByAccount(accountId: String): Flow<Long> =
        dataSource.countByAccount(accountId)

    override suspend fun save(category: Category): Result<Unit> =
        dataSource.insert(category)

    override suspend fun rename(id: String, newName: String): Result<Unit> =
        dataSource.updateName(id, newName)

    override suspend fun archive(id: String): Result<Unit> =
        dataSource.archive(id)

    override suspend fun unarchive(id: String): Result<Unit> =
        dataSource.unarchive(id)
}
