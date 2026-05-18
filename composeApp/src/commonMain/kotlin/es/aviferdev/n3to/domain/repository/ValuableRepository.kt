package es.aviferdev.n3to.domain.repository

import es.aviferdev.n3to.domain.model.Valuable
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para la gestión de bienes muebles de valor (Valuable).
 *
 * Sigue el mismo patrón que [RealEstatePropertyRepository]:
 * - CRUD básico del bien
 * - Acciones específicas: vender, actualizar valor estimado, vincular préstamo
 * - Flujos reactivos por cuenta
 */
interface ValuableRepository {
    /** Obtiene todos los bienes de una cuenta (activos y vendidos). */
    fun getByAccount(accountId: String): Flow<List<Valuable>>

    /** Obtiene solo los bienes activos (no vendidos) de una cuenta. */
    fun getActiveByAccount(accountId: String): Flow<List<Valuable>>

    /** Obtiene un bien por su ID. */
    fun getById(id: String): Flow<Valuable?>

    /** Guarda o actualiza un bien. */
    suspend fun save(valuable: Valuable): Result<Unit>

    /** Marca un bien como vendido. */
    suspend fun sell(id: String, saleDate: Long, salePrice: Double): Result<Unit>

    /** Actualiza el valor estimado de un bien en stock. */
    suspend fun updateEstimatedValue(id: String, value: Double): Result<Unit>

    /** Vincula un préstamo a un bien. */
    suspend fun linkLoan(valuableId: String, loanId: String): Result<Unit>

    /** Elimina un bien y sus transacciones vinculadas. */
    suspend fun delete(id: String): Result<Unit>
}
