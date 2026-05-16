package es.aviferdev.trackfolio.data.repository.realestate

import es.aviferdev.trackfolio.data.datasource.realestate.RealEstatePropertyLocalDataSource
import es.aviferdev.trackfolio.domain.model.RealEstateProperty
import es.aviferdev.trackfolio.domain.repository.RealEstatePropertyRepository
import kotlinx.coroutines.flow.Flow

class RealEstatePropertyRepositoryImpl(
    private val localDataSource: RealEstatePropertyLocalDataSource
) : RealEstatePropertyRepository {

    override fun getPropertiesByAccount(accountId: String): Flow<List<RealEstateProperty>> =
        localDataSource.getByAccount(accountId)

    override fun getActiveByAccount(accountId: String): Flow<List<RealEstateProperty>> =
        localDataSource.getActiveByAccount(accountId)

    override fun getPropertyById(id: String): Flow<RealEstateProperty?> =
        localDataSource.getById(id)

    override suspend fun saveProperty(property: RealEstateProperty): Result<Unit> =
        localDataSource.insert(property)

    override suspend fun updateEstimatedValue(id: String, value: Double): Result<Unit> =
        localDataSource.updateEstimatedValue(id, value)

    override suspend fun archiveProperty(id: String): Result<Unit> =
        localDataSource.archive(id)

    override suspend fun linkLoan(propertyId: String, loanId: String): Result<Unit> =
        localDataSource.updateLinkedLoan(propertyId, loanId)

    override suspend fun dismissMortgageReminder(propertyId: String): Result<Unit> =
        localDataSource.dismissMortgageReminder(propertyId)
}
