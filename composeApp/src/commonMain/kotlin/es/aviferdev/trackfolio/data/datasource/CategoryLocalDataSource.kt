package es.aviferdev.trackfolio.data.datasource

import es.aviferdev.trackfolio.data.database.CategoryEntity
import kotlinx.coroutines.flow.Flow

interface CategoryLocalDataSource {
    /** Devuelve únicamente categorías activas (no archivadas). */
    fun getAll(): Flow<List<CategoryEntity>>
    fun getByType(type: String): Flow<List<CategoryEntity>>

    /** Incluye archivadas. Úsalo para resolver nombres de categorías referenciadas
     *  por transacciones existentes (no para selectores de UI). */
    fun getAllIncludingArchived(): Flow<List<CategoryEntity>>

    fun getById(id: String): Flow<CategoryEntity?>
    fun count(): Flow<Long>

    suspend fun insert(entity: CategoryEntity): Result<Unit>
    suspend fun updateName(id: String, name: String): Result<Unit>
    /** Soft delete: marca la categoría como archivada conservando el vínculo
     *  con las transacciones existentes. */
    suspend fun archive(id: String): Result<Unit>
    suspend fun unarchive(id: String): Result<Unit>
}
