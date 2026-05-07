package es.aviferdev.trackfolio.data.datasource.transaction

import es.aviferdev.trackfolio.data.database.CategoryEntity
import kotlinx.coroutines.flow.Flow

interface TransactionCategoryLocalDataSource {
    fun getAll(): Flow<List<CategoryEntity>>
    fun getByType(type: String): Flow<List<CategoryEntity>>
    fun getAllIncludingArchived(): Flow<List<CategoryEntity>>
    fun getById(id: String): Flow<CategoryEntity?>
    fun count(): Flow<Long>
    suspend fun insert(entity: CategoryEntity): Result<Unit>
    suspend fun updateName(id: String, name: String): Result<Unit>
    suspend fun archive(id: String): Result<Unit>
    suspend fun unarchive(id: String): Result<Unit>
}
