package es.aviferdev.n3to.data.datasource.valuable

import es.aviferdev.n3to.domain.model.Valuable
import kotlinx.coroutines.flow.Flow

/**
 * Fuente de datos local para bienes (Valuable).
 * Sigue el mismo patrón que [es.aviferdev.n3to.data.datasource.realestate.RealEstatePropertyLocalDataSource].
 */
interface ValuableLocalDataSource {
    /** Obtiene todos los bienes de una cuenta (activos y vendidos). */
    fun getByAccount(accountId: String): Flow<List<Valuable>>

    /** Obtiene solo los bienes activos (no vendidos) de una cuenta. */
    fun getActiveByAccount(accountId: String): Flow<List<Valuable>>

    /** Obtiene un bien por su ID. */
    fun getById(id: String): Flow<Valuable?>

    /** Guarda o actualiza un bien. */
    suspend fun insert(valuable: Valuable): Result<Unit>

    /** Marca un bien como vendido. */
    suspend fun sell(id: String, saleDate: Long, salePrice: Double): Result<Unit>

    /** Actualiza el valor estimado de un bien. */
    suspend fun updateEstimatedValue(id: String, value: Double): Result<Unit>

    /** Vincula un préstamo a un bien. */
    suspend fun updateLinkedLoan(valuableId: String, loanId: String): Result<Unit>

    /** Archiva un bien (soft-delete). */
    suspend fun archive(id: String): Result<Unit>

    /** Desarchiva un bien. */
    suspend fun unarchive(id: String): Result<Unit>

    /** Elimina un bien físicamente. */
    suspend fun delete(id: String): Result<Unit>
}
