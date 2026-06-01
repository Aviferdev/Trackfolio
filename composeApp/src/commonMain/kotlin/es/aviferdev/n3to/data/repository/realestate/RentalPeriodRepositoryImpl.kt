package es.aviferdev.n3to.data.repository.realestate

import es.aviferdev.n3to.data.datasource.realestate.RealEstatePropertyLocalDataSource
import es.aviferdev.n3to.domain.model.RentalPeriod
import es.aviferdev.n3to.domain.repository.RentalPeriodRepository
import kotlinx.coroutines.flow.Flow

class RentalPeriodRepositoryImpl(
    private val propertyDataSource: RealEstatePropertyLocalDataSource
) : RentalPeriodRepository {

    override fun getRentalPeriodsByProperty(propertyId: String): Flow<List<RentalPeriod>> =
        propertyDataSource.getRentalPeriodsByProperty(propertyId)

    override fun getActivePeriodByProperty(propertyId: String): Flow<RentalPeriod?> =
        propertyDataSource.getActiveRentalPeriodByProperty(propertyId)

    override suspend fun openRentalPeriod(period: RentalPeriod): Result<Unit> =
        propertyDataSource.insertRentalPeriod(period)

    override suspend fun closeRentalPeriod(periodId: String, endDate: Long): Result<Unit> =
        propertyDataSource.closeRentalPeriod(periodId, endDate)
}
