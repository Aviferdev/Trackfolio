package es.aviferdev.n3to.data.datasource.transaction

import es.aviferdev.n3to.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface TransactionCategoryLocalDataSource {
    fun getByAccount(accountId: String): Flow<List<Category>>
    fun getByTypeAndAccount(accountId: String, type: String): Flow<List<Category>>
    fun getAllIncludingArchivedByAccount(accountId: String): Flow<List<Category>>
    fun getByTypeIncludingArchivedByAccount(
        accountId: String,
        type: String
    ): Flow<List<Category>>

    fun getById(id: String): Flow<Category?>
    fun countByAccount(accountId: String): Flow<Long>
    suspend fun insert(entity: Category): Result<Unit>
    suspend fun updateName(id: String, name: String): Result<Unit>
    suspend fun archive(id: String): Result<Unit>
    suspend fun unarchive(id: String): Result<Unit>
}
