package es.aviferdev.n3to.data.datasource.transaction

import es.aviferdev.n3to.data.database.TransactionEntity
import es.aviferdev.n3to.data.database.TransactionLinkEntity
import es.aviferdev.n3to.data.database.IncomeTaxDetailsEntity
import es.aviferdev.n3to.domain.model.AnnualSummary
import es.aviferdev.n3to.domain.model.CategoryBreakdown
import es.aviferdev.n3to.domain.model.IncomeTypeBreakdown
import es.aviferdev.n3to.domain.model.MonthlyTotals
import es.aviferdev.n3to.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionLocalDataSource {
    fun getByMonthAndAccount(
        accountId: String,
        year: String,
        month: String
    ): Flow<List<Transaction>>

    fun getMonthlyTotalsByAccount(
        accountId: String,
        year: String,
        month: String
    ): Flow<MonthlyTotals>

    fun getAnnualTotalsByAccount(accountId: String, year: String): Flow<AnnualSummary>
    fun getRecentByAccount(accountId: String, limit: Long): Flow<List<Transaction>>
    fun getMonthlyBreakdown(accountId: String, year: String): Flow<List<MonthlyTotals>>

    /** Todos los ingresos del año indicado para el informe fiscal. */
    fun getIncomeByYear(accountId: String, year: String): Flow<List<Transaction>>

    /** Desglose de gastos por categoría para un año. */
    fun getExpensesByCategoryPerYear(accountId: String, year: String): Flow<List<CategoryBreakdown>>

    /** Desglose de gastos por categoría para un mes. */
    fun getExpensesByCategoryPerMonth(accountId: String, year: String, month: String): Flow<List<CategoryBreakdown>>

    /** Desglose de ingresos por tipo para un año. */
    fun getIncomeByTypePerYear(accountId: String, year: String): Flow<List<IncomeTypeBreakdown>>

    /** Desglose de ingresos por tipo para un mes. */
    fun getIncomeByTypePerMonth(accountId: String, year: String, month: String): Flow<List<IncomeTypeBreakdown>>

    /** Obtiene una transacción por su ID. */
    fun getById(id: String): Flow<Transaction?>

    // CRUD
    suspend fun insert(entity: TransactionEntity): Result<Unit>
    suspend fun update(entity: TransactionEntity): Result<Unit>
    suspend fun delete(id: String): Result<Unit>

    // Tax Details
    suspend fun insertTaxDetails(details: IncomeTaxDetailsEntity): Result<Unit>
    suspend fun updateTaxDetails(details: IncomeTaxDetailsEntity): Result<Unit>
    suspend fun deleteTaxDetails(transactionId: String): Result<Unit>

    // Links
    suspend fun insertLink(link: TransactionLinkEntity): Result<Unit>
    suspend fun deleteLinksByTransaction(transactionId: String): Result<Unit>
    suspend fun deleteByLinkTypeAndEntityId(linkType: String, entityId: String): Result<Unit>

    // Queries por link
    fun getByLinkTypeAndEntityId(linkType: String, entityId: String): Flow<Transaction?>
    fun getByAssetIdAndLinkType(assetId: String, linkType: String): Flow<List<Transaction>>
    fun getByLinkedProperty(propertyId: String): Flow<List<Transaction>>
    fun getByLinkedValuable(valuableId: String): Flow<List<Transaction>>
    fun getOldestDate(accountId: String): Flow<Long?>
}
