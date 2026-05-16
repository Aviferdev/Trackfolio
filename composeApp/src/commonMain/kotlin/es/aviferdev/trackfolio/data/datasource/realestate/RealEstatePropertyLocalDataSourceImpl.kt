package es.aviferdev.trackfolio.data.datasource.realestate

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.trackfolio.data.database.TrackfolioDatabase
import es.aviferdev.trackfolio.data.database.mapper.toDomain
import es.aviferdev.trackfolio.data.database.mapper.toEntity
import es.aviferdev.trackfolio.domain.model.RealEstateProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RealEstatePropertyLocalDataSourceImpl(
    private val database: TrackfolioDatabase
) : RealEstatePropertyLocalDataSource {

    private val queries = database.realEstatePropertyQueries

    override fun getByAccount(accountId: String): Flow<List<RealEstateProperty>> =
        queries.selectByAccount(accountId)
            .asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getActiveByAccount(accountId: String): Flow<List<RealEstateProperty>> =
        queries.selectActiveByAccount(accountId)
            .asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getById(id: String): Flow<RealEstateProperty?> =
        queries.selectById(id)
            .asFlow().mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override suspend fun insert(property: RealEstateProperty): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val e = property.toEntity()
            queries.insert(
                id                      = e.id,
                accountId               = e.accountId,
                name                    = e.name,
                address                 = e.address,
                propertyType            = e.propertyType,
                purchaseValue           = e.purchaseValue,
                currentEstimatedValue   = e.currentEstimatedValue,
                acquisitionDate         = e.acquisitionDate,
                ownershipPercentage     = e.ownershipPercentage,
                linkedLoanId            = e.linkedLoanId,
                rentalStatus            = e.rentalStatus,
                monthlyRent             = e.monthlyRent,
                mortgageReminderDismissed = e.mortgageReminderDismissed,
                archived                = e.archived
            )
        }
    }

    override suspend fun updateEstimatedValue(id: String, value: Double): Result<Unit> =
        runCatching { withContext(Dispatchers.IO) { queries.updateEstimatedValue(value = value, id = id) } }

    override suspend fun updateLinkedLoan(id: String, loanId: String): Result<Unit> =
        runCatching { withContext(Dispatchers.IO) { queries.updateLinkedLoan(loanId = loanId, id = id) } }

    override suspend fun dismissMortgageReminder(id: String): Result<Unit> =
        runCatching { withContext(Dispatchers.IO) { queries.dismissMortgageReminder(id) } }

    override suspend fun archive(id: String): Result<Unit> =
        runCatching { withContext(Dispatchers.IO) { queries.archive(id) } }
}
