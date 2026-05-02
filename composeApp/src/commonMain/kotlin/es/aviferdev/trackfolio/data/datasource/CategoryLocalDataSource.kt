package es.aviferdev.trackfolio.data.datasource

import es.aviferdev.trackfolio.data.database.CategoryEntity
import kotlinx.coroutines.flow.Flow

interface CategoryLocalDataSource {
    fun getAll(): Flow<List<CategoryEntity>>
    fun getByType(type: String): Flow<List<CategoryEntity>>
    fun getById(id: String): Flow<CategoryEntity?>
    fun count(): Flow<Long>
    suspend fun insert(entity: CategoryEntity): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
}
