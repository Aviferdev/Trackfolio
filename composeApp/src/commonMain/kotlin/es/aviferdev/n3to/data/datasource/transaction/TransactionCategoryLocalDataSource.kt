package es.aviferdev.n3to.data.datasource.transaction

import es.aviferdev.n3to.data.database.CategoryEntity
import kotlinx.coroutines.flow.Flow

interface TransactionCategoryLocalDataSource {
    fun getByAccount(accountId: String): Flow<List<CategoryEntity>>
    fun getByTypeAndAccount(accountId: String, type: String): Flow<List<CategoryEntity>>
    fun getAllIncludingArchivedByAccount(accountId: String): Flow<List<CategoryEntity>>
    fun getByTypeIncludingArchivedByAccount(
        accountId: String,
        type: String
    ): Flow<List<CategoryEntity>>

    fun getById(id: String): Flow<CategoryEntity?>
    fun countByAccount(accountId: String): Flow<Long>
    suspend fun insert(entity: CategoryEntity): Result<Unit>
    suspend fun updateName(id: String, name: String): Result<Unit>
    suspend fun archive(id: String): Result<Unit>
    suspend fun unarchive(id: String): Result<Unit>
}
