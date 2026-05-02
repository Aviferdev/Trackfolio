package es.aviferdev.trackfolio.domain.repository

import es.aviferdev.trackfolio.domain.model.Category
import es.aviferdev.trackfolio.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAll(): Flow<List<Category>>
    fun getByType(type: TransactionType): Flow<List<Category>>
    fun count(): Flow<Long>
    suspend fun save(category: Category): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
}
