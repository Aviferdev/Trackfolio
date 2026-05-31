package es.aviferdev.n3to.data.datasource.transaction

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.n3to.data.database.N3toDatabase
import es.aviferdev.n3to.data.database.TransactionEntity
import es.aviferdev.n3to.data.database.TransactionLinkEntity
import es.aviferdev.n3to.data.database.IncomeTaxDetailsEntity
import es.aviferdev.n3to.data.database.mapper.toDomain
import es.aviferdev.n3to.data.database.mapper.toEntity
import es.aviferdev.n3to.domain.model.AnnualSummary
import es.aviferdev.n3to.domain.model.CategoryBreakdown
import es.aviferdev.n3to.domain.model.IncomeTaxDetails
import es.aviferdev.n3to.domain.model.IncomeTypeBreakdown
import es.aviferdev.n3to.domain.model.MonthlyTotals
import es.aviferdev.n3to.domain.model.TaxLine
import es.aviferdev.n3to.domain.model.TaxRole
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.TransactionLink
import es.aviferdev.n3to.domain.model.TransactionLinkType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TransactionLocalDataSourceImpl(
    private val database: N3toDatabase
) : TransactionLocalDataSource {

    private val queries = database.transactionQueries
    private val taxLineQueries = database.taxLineQueries
    private val linkQueries = database.transactionLinkQueries
    private val taxDetailsQueries = database.incomeTaxDetailsQueries

    private fun loadTaxLines(transactionId: String): List<TaxLine> =
        taxLineQueries.selectByTransaction(transactionId).executeAsList().map { entity ->
            TaxLine(
                name = entity.name,
                role = TaxRole.valueOf(entity.role),
                percent = entity.percent,
                amount = entity.amount
            )
        }

    private fun loadLinks(transactionId: String): List<TransactionLink> =
        linkQueries.selectByTransaction(transactionId).executeAsList().map { entity ->
            TransactionLink(
                id = entity.id,
                linkType = TransactionLinkType.valueOf(entity.linkType),
                linkedEntityId = entity.linkedEntityId,
                assetId = entity.assetId
            )
        }

    private fun loadTaxDetails(transactionId: String): IncomeTaxDetails? =
        taxDetailsQueries.selectByTransaction(transactionId).executeAsOneOrNull()?.let { entity ->
            val issuerName = entity.issuerId?.let {
                database.issuerQueries.selectById(it).executeAsOneOrNull()?.name
            }
            entity.toDomain(issuerName)
        }

    private fun TransactionEntity.toDomainWithDeps(): Transaction =
        toDomain(
            taxLines = loadTaxLines(id),
            links = loadLinks(id),
            taxDetails = loadTaxDetails(id)
        )

    private fun List<TransactionEntity>.toDomainWithDeps(): List<Transaction> =
        map { entity ->
            entity.toDomain(
                taxLines = loadTaxLines(entity.id),
                links = loadLinks(entity.id),
                taxDetails = loadTaxDetails(entity.id)
            )
        }

    override fun getById(id: String): Flow<Transaction?> =
        queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomainWithDeps() }

    override fun getByMonthAndAccount(
        accountId: String, year: String, month: String
    ): Flow<List<Transaction>> =
        queries.selectByMonthAndAccount(accountId, year, month)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.toDomainWithDeps() }

    override fun getMonthlyTotalsByAccount(
        accountId: String, year: String, month: String
    ): Flow<MonthlyTotals> =
        queries.getMonthlyTotalsByAccount(accountId, year, month)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { row ->
                MonthlyTotals(
                    year = year,
                    month = month,
                    totalIncome = row?.totalIncome ?: 0.0,
                    totalExpense = row?.totalExpense ?: 0.0
                )
            }

    override fun getAnnualTotalsByAccount(
        accountId: String, year: String
    ): Flow<AnnualSummary> {
        val prevYear = (year.toInt() - 1).toString()
        return queries.getAnnualTotalsByAccount(accountId, year)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { current ->
                val prev = queries.getAnnualTotalsByAccount(accountId, prevYear)
                    .executeAsOneOrNull()
                AnnualSummary(
                    year = year,
                    totalIncome = current?.totalIncome ?: 0.0,
                    totalExpense = current?.totalExpense ?: 0.0,
                    previousYearIncome = prev?.totalIncome ?: 0.0,
                    previousYearExpense = prev?.totalExpense ?: 0.0
                )
            }
    }

    override fun getRecentByAccount(accountId: String, limit: Long): Flow<List<Transaction>> =
        queries.selectRecentByAccount(accountId, limit)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.toDomainWithDeps() }

    override fun getMonthlyBreakdown(accountId: String, year: String): Flow<List<MonthlyTotals>> =
        queries.getMonthlyBreakdownByAccount(accountId, year)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    MonthlyTotals(
                        year = year,
                        month = row.month,
                        totalIncome = row.totalIncome,
                        totalExpense = row.totalExpense
                    )
                }
            }

    override fun getIncomeByYear(accountId: String, year: String): Flow<List<Transaction>> =
        queries.getIncomeByYear(accountId, year)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.toDomainWithDeps() }

    override suspend fun insert(entity: TransactionEntity): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.insert(
                    id = entity.id,
                    accountId = entity.accountId,
                    amount = entity.amount,
                    type = entity.type,
                    categoryId = entity.categoryId,
                    date = entity.date,
                    notes = entity.notes,
                    createdAt = entity.createdAt,
                    excludeFromFiscal = entity.excludeFromFiscal,
                    originalCurrency = entity.originalCurrency,
                    originalAmount = entity.originalAmount,
                    exchangeRate = entity.exchangeRate,
                    year = entity.year,
                    month = entity.month
                )
            }
        }

    override suspend fun update(entity: TransactionEntity): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.update(
                    accountId = entity.accountId,
                    amount = entity.amount,
                    type = entity.type,
                    categoryId = entity.categoryId,
                    date = entity.date,
                    notes = entity.notes,
                    excludeFromFiscal = entity.excludeFromFiscal,
                    originalCurrency = entity.originalCurrency,
                    originalAmount = entity.originalAmount,
                    exchangeRate = entity.exchangeRate,
                    year = entity.year,
                    month = entity.month,
                    id = entity.id
                )
            }
        }

    override suspend fun delete(id: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.delete(id)
            }
        }

    // ─── Tax Details ──────────────────────────────────────────────────────────

    override suspend fun insertTaxDetails(details: IncomeTaxDetailsEntity): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                taxDetailsQueries.insert(
                    transactionId = details.transactionId,
                    incomeType = details.incomeType,
                    grossAmount = details.grossAmount,
                    commissionAmount = details.commissionAmount,
                    issuerId = details.issuerId
                )
            }
        }

    override suspend fun updateTaxDetails(details: IncomeTaxDetailsEntity): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                taxDetailsQueries.update(
                    incomeType = details.incomeType,
                    grossAmount = details.grossAmount,
                    commissionAmount = details.commissionAmount,
                    issuerId = details.issuerId,
                    transactionId = details.transactionId
                )
            }
        }

    override suspend fun deleteTaxDetails(transactionId: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                taxDetailsQueries.delete(transactionId)
            }
        }

    // ─── Links ────────────────────────────────────────────────────────────────

    override suspend fun insertLink(link: TransactionLinkEntity): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                linkQueries.insert(
                    id = link.id,
                    transactionId = link.transactionId,
                    linkType = link.linkType,
                    linkedEntityId = link.linkedEntityId,
                    assetId = link.assetId
                )
            }
        }

    override suspend fun deleteLinksByTransaction(transactionId: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                linkQueries.deleteByTransaction(transactionId)
            }
        }

    override suspend fun deleteByLinkTypeAndEntityId(linkType: String, entityId: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.deleteByLinkedAssetTransaction(entityId)
            }
        }

    override fun getByLinkTypeAndEntityId(linkType: String, entityId: String): Flow<Transaction?> =
        when (linkType) {
            "ASSET_TRANSACTION" -> queries.selectByLinkedAssetTransaction(entityId)
            "PROPERTY" -> queries.selectByLinkedProperty(entityId)
            "VALUABLE" -> queries.selectByLinkedValuable(entityId)
            else -> throw IllegalArgumentException("Unsupported linkType: $linkType")
        }
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomainWithDeps() }

    override fun getByAssetIdAndLinkType(assetId: String, linkType: String): Flow<List<Transaction>> =
        when (linkType) {
            "DIVIDEND" -> queries.getDividendsByAssetId(assetId)
            else -> throw IllegalArgumentException("Unsupported linkType for asset query: $linkType")
        }
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.toDomainWithDeps() }

    override fun getByLinkedProperty(propertyId: String): Flow<List<Transaction>> =
        queries.selectByLinkedProperty(propertyId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.toDomainWithDeps() }

    override fun getByLinkedValuable(valuableId: String): Flow<List<Transaction>> =
        queries.selectByLinkedValuable(valuableId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.toDomainWithDeps() }

    override fun getOldestDate(accountId: String): Flow<Long?> =
        queries.getOldestDateByAccount(accountId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.oldestDate }

    override fun getExpensesByCategoryPerYear(
        accountId: String,
        year: String
    ): Flow<List<CategoryBreakdown>> =
        queries.getExpensesByCategoryPerYear(accountId, year)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    CategoryBreakdown(
                        categoryId = row.categoryId,
                        categoryName = row.categoryName,
                        amount = row.totalAmount ?: 0.0
                    )
                }
            }

    override fun getIncomeByTypePerYear(
        accountId: String,
        year: String
    ): Flow<List<IncomeTypeBreakdown>> =
        queries.getIncomeByTypePerYear(accountId, year)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    val incomeType = es.aviferdev.n3to.domain.model.IncomeType.fromName(row.incomeType)
                    IncomeTypeBreakdown(
                        incomeType = row.incomeType ?: "UNKNOWN",
                        label = incomeType?.label ?: "Otro",
                        emoji = incomeType?.emoji ?: "💰",
                        amount = row.totalAmount ?: 0.0
                    )
                }
            }

    override fun getExpensesByCategoryPerMonth(
        accountId: String,
        year: String,
        month: String
    ): Flow<List<CategoryBreakdown>> =
        queries.getExpensesByCategoryPerMonth(accountId, year, month)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    CategoryBreakdown(
                        categoryId = row.categoryId,
                        categoryName = row.categoryName,
                        amount = row.totalAmount ?: 0.0
                    )
                }
            }

    override fun getIncomeByTypePerMonth(
        accountId: String,
        year: String,
        month: String
    ): Flow<List<IncomeTypeBreakdown>> =
        queries.getIncomeByTypePerMonth(accountId, year, month)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    val incomeType = es.aviferdev.n3to.domain.model.IncomeType.fromName(row.incomeType)
                    IncomeTypeBreakdown(
                        incomeType = row.incomeType ?: "UNKNOWN",
                        label = incomeType?.label ?: "Otro",
                        emoji = incomeType?.emoji ?: "💰",
                        amount = row.totalAmount ?: 0.0
                    )
                }
            }
}
