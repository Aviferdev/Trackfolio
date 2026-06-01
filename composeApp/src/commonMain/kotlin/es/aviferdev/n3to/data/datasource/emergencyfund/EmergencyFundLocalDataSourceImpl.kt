package es.aviferdev.n3to.data.datasource.emergencyfund

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.n3to.data.database.N3toDatabase
import es.aviferdev.n3to.data.database.mapper.toDomain
import es.aviferdev.n3to.data.database.mapper.toEntity
import es.aviferdev.n3to.domain.model.EmergencyFund
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Implementación de [EmergencyFundLocalDataSource] sobre SQLDelight.
 *
 * Las categorías excluidas se almacenan en una tabla N:M normalizada
 * ([EmergencyFundExcludedCategoryEntity]) en lugar de un campo TEXT
 * con IDs separados por comas.
 */
class EmergencyFundLocalDataSourceImpl(
    private val database: N3toDatabase
) : EmergencyFundLocalDataSource {

    private val queries = database.emergencyFundQueries
    private val excludedQueries = database.emergencyFundExcludedCategoryQueries

    override fun getEmergencyFund(accountId: String): Flow<EmergencyFund?> =
        queries.selectByAccount(accountId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { entity ->
                entity?.let { e ->
                    val excludedIds = excludedQueries.selectByAccount(e.accountId).executeAsList()
                    e.toDomain(excludedIds)
                }
            }

    override suspend fun saveEmergencyFund(fund: EmergencyFund) {
        withContext(Dispatchers.IO) {
            val e = fund.toEntity()
            queries.insertOrReplace(
                accountId = e.accountId,
                targetMonths = e.targetMonths,
                calculationMethod = e.calculationMethod,
                manualMonthlyExpense = e.manualMonthlyExpense
            )
            // Actualizar categorías excluidas en la tabla normalizada
            setExcludedCategoriesSync(fund.accountId, fund.excludedCategoryIds)
        }
    }

    override suspend fun deleteEmergencyFund(accountId: String) {
        withContext(Dispatchers.IO) {
            excludedQueries.deleteByAccount(accountId)
            queries.deleteByAccount(accountId)
        }
    }

    override suspend fun getExcludedCategoryIds(accountId: String): List<String> =
        withContext(Dispatchers.IO) {
            excludedQueries.selectByAccount(accountId).executeAsList()
        }

    override suspend fun setExcludedCategories(accountId: String, categoryIds: List<String>) {
        withContext(Dispatchers.IO) {
            setExcludedCategoriesSync(accountId, categoryIds)
        }
    }

    override suspend fun addExcludedCategory(accountId: String, categoryId: String) {
        withContext(Dispatchers.IO) {
            excludedQueries.insert(accountId, categoryId)
        }
    }

    override suspend fun removeExcludedCategory(accountId: String, categoryId: String) {
        withContext(Dispatchers.IO) {
            excludedQueries.deleteByAccountAndCategory(accountId, categoryId)
        }
    }

    private fun setExcludedCategoriesSync(accountId: String, categoryIds: List<String>) {
        excludedQueries.deleteByAccount(accountId)
        categoryIds.forEach { catId ->
            excludedQueries.insert(accountId, catId)
        }
    }
}
