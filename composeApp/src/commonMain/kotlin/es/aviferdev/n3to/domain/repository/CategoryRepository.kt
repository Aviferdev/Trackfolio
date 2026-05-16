package es.aviferdev.n3to.domain.repository

import es.aviferdev.n3to.domain.model.Category
import es.aviferdev.n3to.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    /** Solo categorías activas. */
    fun getAll(): Flow<List<Category>>
    fun getByType(type: TransactionType): Flow<List<Category>>

    /** Incluye archivadas. Úsalo cuando necesites resolver el nombre de
     *  categorías históricas vinculadas a transacciones existentes. */
    fun getAllIncludingArchived(): Flow<List<Category>>

    fun count(): Flow<Long>
    suspend fun save(category: Category): Result<Unit>
    suspend fun rename(id: String, newName: String): Result<Unit>
    suspend fun archive(id: String): Result<Unit>
    suspend fun unarchive(id: String): Result<Unit>
}
