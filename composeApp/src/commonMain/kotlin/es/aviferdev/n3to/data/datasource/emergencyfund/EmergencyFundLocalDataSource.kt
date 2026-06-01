package es.aviferdev.n3to.data.datasource.emergencyfund

import es.aviferdev.n3to.domain.model.EmergencyFund
import kotlinx.coroutines.flow.Flow

/**
 * Fuente de datos local para el fondo de emergencia.
 * Las categorías excluidas se almacenan en una tabla N:M normalizada
 * ([EmergencyFundExcludedCategoryEntity]) en lugar de un campo TEXT con IDs separados.
 */
interface EmergencyFundLocalDataSource {
    fun getEmergencyFund(accountId: String): Flow<EmergencyFund?>
    suspend fun saveEmergencyFund(fund: EmergencyFund)
    suspend fun deleteEmergencyFund(accountId: String)
    suspend fun getExcludedCategoryIds(accountId: String): List<String>
    suspend fun setExcludedCategories(accountId: String, categoryIds: List<String>)
    suspend fun addExcludedCategory(accountId: String, categoryId: String)
    suspend fun removeExcludedCategory(accountId: String, categoryId: String)
}
